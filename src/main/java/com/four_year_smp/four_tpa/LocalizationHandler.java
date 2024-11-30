package com.four_year_smp.four_tpa;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import org.yaml.snakeyaml.Yaml;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class LocalizationHandler {
    private final HashMap<String, String> _translations = new HashMap<>();
    private final Semaphore _translationLock = new Semaphore(1);
    private final FourTpaPlugin _plugin;

    public LocalizationHandler(FourTpaPlugin plugin) {
        _plugin = plugin;
        reload();
    }

    public Component getReloaded() {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("reloaded"));
    }

    public Component getPlayersOnly() {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("players_only"));
    }

    public Component getPlayerNotFound(String playerName) {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("player_not_found", playerName));
    }

    public Component getPlayerWentOffline(String player) {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("player_went_offline", player));
    }

    public Component getPlayerIsOnline(String player) {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("player_is_online", player));
    }

    public Component getPlayerHasNotPlayedBefore(String player) {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("player_has_not_played_before", player));
    }

    public Component getPlayerBackMissing() {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("player_back_missing"));
    }

    public Component getPlayerBackTeleported() {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("player_back_teleported"));
    }

    public Component getTpaDenyUsage() {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("tpa_deny_usage"));
    }

    public Component getTpaAcceptUsage() {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("tpa_accept_usage"));
    }

    public Component getTpaCancelUsage() {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("tpa_cancel_usage"));
    }

    public Component getTpaNone() {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("tpa_none"));
    }

    public Component getTpaSenderSend(String player, int seconds) {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("tpa_sender_send", player, seconds));
    }

    public Component getTpaSenderExpired(String player) {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("tpa_sender_expired", player));
    }

    public Component getTpaSenderConflict(String player) {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("tpa_sender_conflict", player));
    }

    public Component getTpaSenderCancel(String player) {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("tpa_sender_cancel", player));
    }

    public Component getTpaSenderAccepted(String player) {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("tpa_sender_accepted", player));
    }

    public Component getTpaSenderDenied(String player) {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("tpa_sender_denied", player));
    }

    public Component getTpaSenderAcceptAll(int count) {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("tpa_sender_all", count));
    }

    public Component getTpaReceiverReceive(String player, int seconds) {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("tpa_receiver_receive", player, seconds));
    }

    public Component getTpaReceiverAccept(String player) {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("tpa_receiver_accept", player));
    }

    public Component getTpaReceiverDeny(String player) {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("tpa_receiver_deny", player));
    }

    public Component getTpaReceiverExpired(String player) {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("tpa_receiver_expired", player));
    }

    public Component getTpaReceiverCancel(String player) {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("tpa_receiver_cancel", player));
    }

    public Component getTpaReceiverNotFound(String player) {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("tpa_receiver_not_found", player));
    }

    public Component getTpaHereSender(String player, int seconds) {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("tpa_here_sender", player, seconds));
    }

    public Component getTpaHereReceiver(String player, int seconds) {
        return LegacyComponentSerializer.legacy('&').deserialize(getTranslation("tpa_here_receiver", player, seconds));
    }

    public void reload() {
        _translationLock.acquireUninterruptibly();

        try {
            // Ensure the translation files exist
            ensureTranslationFilesExist();

            // Clear the existing translations
            _translations.clear();

            // Get the current language file
            String langFile = _plugin.getConfig().getString("lang", "en_US");

            // Iterate over all the keys in the translation file and add them to the translations map
            Path langFilePath = _plugin.getDataFolder().toPath().resolve("lang").resolve(langFile + ".yml");

            InputStream translationStream;
            try {
                translationStream = Files.newInputStream(langFilePath, StandardOpenOption.READ);
            } catch (IOException error) {
                _plugin.getLogger().severe(MessageFormat.format("Failed to open translation file, falling back to built-in translations: {0}", error));
                translationStream = _plugin.getResource("lang/en_US.yml");
            }

            // Load the translations from the file
            Yaml yaml = new Yaml();
            Map<?, ?> kvp = yaml.load(translationStream);
            for (Map.Entry<?, ?> entry : kvp.entrySet()) {
                _translations.put(entry.getKey().toString(), entry.getValue().toString());
                _plugin.logDebug(MessageFormat.format("Loaded translation: {0} -> {1}", entry.getKey(), entry.getValue()));
            }

            try {
                translationStream.close();
            } catch (IOException error) {
                _plugin.getLogger().severe(MessageFormat.format("Failed to close translation stream: {0}", error.getMessage()));
            }
        } finally {
            _translationLock.release();
        }
    }

    private String getTranslation(String key, Object... args) {
        _translationLock.acquireUninterruptibly();
        try {
            String translation = MessageFormat.format(_translations.getOrDefault(key, key), args);
            return MessageFormat.format("{0} {1}", _translations.getOrDefault("prefix", "[4TPA]"), translation);
        } finally {
            _translationLock.release();
        }
    }

    private boolean ensureTranslationFilesExist() {
        // Make sure the data folder exists
        if (!_plugin.getDataFolder().exists() && !_plugin.getDataFolder().mkdirs()) {
            _plugin.getLogger().severe(MessageFormat.format("Failed to create data folder: {0}", _plugin.getDataFolder().getAbsolutePath()));
            return false;
        }

        // Get the path to the translations directory
        Path translationPath = _plugin.getDataFolder().toPath().resolve("translations");

        // Attempt to load the current translation file
        String langFile = _plugin.getConfig().getString("lang", "en_US");
        Path translationFile = translationPath.resolve(langFile + ".yml");
        if (!translationFile.toFile().exists()) {
            // If the file was deleted...
            _plugin.saveResource("lang/" + langFile + ".yml", true);
        }
        return true;
    }
}
