package io.kimconsulting.maxbots.api.request;

import io.kimconsulting.maxbots.api.model.NewMessageBody;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record SendMessageRequest(
    Long userId,
    Long chatId,
    NewMessageBody body
) {
    public SendMessageRequest {
        Objects.requireNonNull(body, "body");
        if (userId == null && chatId == null) {
            throw new IllegalArgumentException("Either userId or chatId must be provided");
        }
    }

    public static SendMessageRequest toUser(long userId, String text) {
        return new SendMessageRequest(userId, null, NewMessageBody.text(text));
    }

    public static SendMessageRequest toUser(long userId, NewMessageBody body) {
        return new SendMessageRequest(userId, null, body);
    }

    public static SendMessageRequest toChat(long chatId, String text) {
        return new SendMessageRequest(null, chatId, NewMessageBody.text(text));
    }

    public static SendMessageRequest toChat(long chatId, NewMessageBody body) {
        return new SendMessageRequest(null, chatId, body);
    }

    public Map<String, Object> toQueryMap() {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("user_id", userId);
        query.put("chat_id", chatId);
        return query;
    }
}
