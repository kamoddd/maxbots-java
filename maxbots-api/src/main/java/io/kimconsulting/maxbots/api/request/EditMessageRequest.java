package io.kimconsulting.maxbots.api.request;

import io.kimconsulting.maxbots.api.model.NewMessageBody;
import java.util.Objects;

public record EditMessageRequest(
    String messageId,
    NewMessageBody body
) {
    public EditMessageRequest {
        Objects.requireNonNull(messageId, "messageId");
        Objects.requireNonNull(body, "body");
    }
}
