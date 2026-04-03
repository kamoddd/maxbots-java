package io.kimconsulting.maxbots.api.request;

import java.util.LinkedHashMap;
import java.util.Map;

public record GetMessagesRequest(
    Long userId,
    Long chatId,
    Integer count,
    String from,
    String to
) {
    public static GetMessagesRequest forUser(long userId) {
        return new GetMessagesRequest(userId, null, null, null, null);
    }

    public static GetMessagesRequest forChat(long chatId) {
        return new GetMessagesRequest(null, chatId, null, null, null);
    }

    public Map<String, Object> toQueryMap() {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("user_id", userId);
        query.put("chat_id", chatId);
        query.put("count", count);
        query.put("from", from);
        query.put("to", to);
        return query;
    }
}
