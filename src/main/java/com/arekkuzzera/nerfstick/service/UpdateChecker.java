package com.arekkuzzera.nerfstick.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UpdateChecker implements Listener {

    private static final String MODRINTH_PROJECT = "nerfstick-remastered";
    private static final String MODRINTH_API = "https://api.modrinth.com/v2/project/" + MODRINTH_PROJECT + "/version";
    private static final String MODRINTH_PAGE = "https://modrinth.com/plugin/" + MODRINTH_PROJECT;

    private static final String GITHUB_OWNER = "AREKKUZZERA";
    private static final String GITHUB_REPO = "nerfstick";
    private static final String GITHUB_API = "https://api.github.com/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/releases/latest";
    private static final String GITHUB_PAGE = "https://github.com/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/releases/latest";

    private static final Pattern VERSION_FIELD = Pattern.compile("\"version_number\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern TAG_FIELD = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");

    private final JavaPlugin plugin;
    private final String currentVersion;
    private final AtomicReference<UpdateResult> latestResult = new AtomicReference<>();

    public UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getPluginMeta().getVersion();
    }

    public void checkAsync() {
        new BukkitRunnable() {
            @Override
            public void run() {
                UpdateResult result = fetchLatest();
                latestResult.set(result);

                if (result == null) {
                    plugin.getLogger().info("Could not check for updates.");
                    return;
                }

                if (result.updateAvailable()) {
                    plugin.getLogger().warning("A new version of Nerfstick is available: " + result.latestVersion()
                            + " (currently running " + currentVersion + "). Download: " + result.downloadPage());
                } else {
                    plugin.getLogger().info("Nerfstick is up to date (" + currentVersion + ").");
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UpdateResult result = latestResult.get();
        if (result == null || !result.updateAvailable()) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("nerfstick.admin") && !player.isOp()) {
            return;
        }

        player.sendMessage(
                Component.text("[Nerfstick] ", TextColor.color(0x55FF55))
                        .append(Component.text("A new version is available: ", TextColor.color(0xFFAA00)))
                        .append(Component.text(result.latestVersion(), TextColor.color(0x55FF55)))
                        .append(Component.text(" (you have " + currentVersion + "). Click to open the download page.",
                                TextColor.color(0xFFAA00)))
                        .clickEvent(ClickEvent.openUrl(result.downloadPage()))
        );
    }

    private UpdateResult fetchLatest() {
        UpdateResult modrinth = tryModrinth();
        return modrinth != null ? modrinth : tryGitHub();
    }

    private UpdateResult tryModrinth() {
        try {
            String body = httpGet(MODRINTH_API);
            Matcher matcher = VERSION_FIELD.matcher(body);
            if (matcher.find()) {
                String latest = matcher.group(1);
                return new UpdateResult(isNewer(latest, currentVersion), latest, MODRINTH_PAGE);
            }
        } catch (Exception e) {
            plugin.getLogger().fine("Modrinth update check failed: " + e.getMessage());
        }

        return null;
    }

    private UpdateResult tryGitHub() {
        try {
            String body = httpGet(GITHUB_API);
            Matcher matcher = TAG_FIELD.matcher(body);
            if (matcher.find()) {
                String tag = matcher.group(1);
                String normalized = tag.startsWith("v") ? tag.substring(1) : tag;
                return new UpdateResult(isNewer(normalized, currentVersion), normalized, GITHUB_PAGE);
            }
        } catch (Exception e) {
            plugin.getLogger().fine("GitHub update check failed: " + e.getMessage());
        }

        return null;
    }

    private String httpGet(String url) throws IOException {
        URLConnection connection = URI.create(url).toURL().openConnection();
        connection.setRequestProperty("User-Agent", "Nerfstick-UpdateChecker/" + currentVersion
                + " (+https://github.com/" + GITHUB_OWNER + "/" + GITHUB_REPO + ")");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        try (InputStream inputStream = connection.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private boolean isNewer(String remote, String local) {
        if (remote.equalsIgnoreCase(local)) {
            return false;
        }

        String[] remoteParts = remote.toLowerCase(Locale.ROOT).split("[.\\-+]");
        String[] localParts = local.toLowerCase(Locale.ROOT).split("[.\\-+]");
        int length = Math.max(remoteParts.length, localParts.length);

        for (int index = 0; index < length; index++) {
            int remotePart = parseIntOrMinusOne(index < remoteParts.length ? remoteParts[index] : "0");
            int localPart = parseIntOrMinusOne(index < localParts.length ? localParts[index] : "0");

            if (remotePart == -1 || localPart == -1) {
                return !remote.equalsIgnoreCase(local);
            }

            if (remotePart != localPart) {
                return remotePart > localPart;
            }
        }

        return false;
    }

    private int parseIntOrMinusOne(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private record UpdateResult(boolean updateAvailable, String latestVersion, String downloadPage) {
    }
}
