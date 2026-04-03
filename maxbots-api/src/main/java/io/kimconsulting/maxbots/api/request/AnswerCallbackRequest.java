package io.kimconsulting.maxbots.api.request;

import io.kimconsulting.maxbots.api.model.NewMessageBody;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record AnswerCallbackRequest(
    String callbackId,
    NewMessageBody message,
    String notification
) {
    public AnswerCallbackRequest {
        Objects.requireNonNull(callbackId, "callbackId");
    }

    public static AnswerCallbackRequest notification(String callbackId, String notification) {
        return new AnswerCallbackRequest(callbackId, null, notification);
    }

    public static AnswerCallbackRequest message(String callbackId, NewMessageBody message) {
        return new AnswerCallbackRequest(callbackId, message, null);
    }

    public Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        body.put("notification", notification);
        return body;
    }
}
