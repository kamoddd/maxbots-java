package io.kimconsulting.maxbots.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kimconsulting.maxbots.api.http.MaxObjectMapperFactory;
import io.kimconsulting.maxbots.api.model.Update;
import io.kimconsulting.maxbots.core.bot.MaxBot;
import java.util.Objects;

public final class MaxWebhookHandler {
    private final ObjectMapper objectMapper;
    private final String expectedSecret;

    public MaxWebhookHandler(String expectedSecret) {
        this(expectedSecret, MaxObjectMapperFactory.create());
    }

    public MaxWebhookHandler(String expectedSecret, ObjectMapper objectMapper) {
        this.expectedSecret = expectedSecret;
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    public Update parse(String body) {
        try {
            return objectMapper.readValue(body, Update.class);
        } catch (JsonProcessingException ex) {
            throw new MaxWebhookException("Unable to parse MAX webhook body", ex);
        }
    }

    public Update handle(String body, String providedSecret, MaxBot bot) {
        verifySecret(providedSecret);
        Update update = parse(body);
        bot.handle(update);
        return update;
    }

    public void verifySecret(String providedSecret) {
        if (expectedSecret == null || expectedSecret.isBlank()) {
            return;
        }
        if (!expectedSecret.equals(providedSecret)) {
            throw new MaxWebhookException("Invalid X-Max-Bot-Api-Secret header");
        }
    }
}
