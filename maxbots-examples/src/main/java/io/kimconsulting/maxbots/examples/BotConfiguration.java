package io.kimconsulting.maxbots.examples;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public record BotConfiguration(
    String token,
    String botName,
    boolean debug
) {
    public BotConfiguration {
        Objects.requireNonNull(token, "token");
        if (token.isBlank()) {
            throw new IllegalStateException("Set max.bot.token in maxbots-examples/config/bot.local.properties before starting the bot");
        }
        Objects.requireNonNull(botName, "botName");
    }

    public static BotConfiguration load() {
        Properties properties = new Properties();
        List<Path> candidates = List.of(
            Path.of("maxbots-examples", "config", "bot.properties.example"),
            Path.of("maxbots-examples", "config", "bot.local.properties"),
            Path.of("config", "bot.properties.example"),
            Path.of("config", "bot.local.properties")
        );

        for (Path path : candidates) {
            if (Files.exists(path)) {
                loadInto(properties, path);
            }
        }

        return new BotConfiguration(
            properties.getProperty("max.bot.token", "").trim(),
            properties.getProperty("max.bot.name", "Kim Consulting Echo Bot").trim(),
            Boolean.parseBoolean(properties.getProperty("max.bot.debug", "false").trim())
        );
    }

    private static void loadInto(Properties properties, Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load config from " + path, ex);
        }
    }
}
