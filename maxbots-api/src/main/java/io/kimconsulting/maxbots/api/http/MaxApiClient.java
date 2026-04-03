package io.kimconsulting.maxbots.api.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kimconsulting.maxbots.api.model.ActionResult;
import io.kimconsulting.maxbots.api.model.BotInfo;
import io.kimconsulting.maxbots.api.model.Message;
import io.kimconsulting.maxbots.api.model.MessagesResponse;
import io.kimconsulting.maxbots.api.model.Subscription;
import io.kimconsulting.maxbots.api.model.SubscriptionsResponse;
import io.kimconsulting.maxbots.api.model.UpdatesPage;
import io.kimconsulting.maxbots.api.request.AnswerCallbackRequest;
import io.kimconsulting.maxbots.api.request.CreateSubscriptionRequest;
import io.kimconsulting.maxbots.api.request.EditMessageRequest;
import io.kimconsulting.maxbots.api.request.GetMessagesRequest;
import io.kimconsulting.maxbots.api.request.GetUpdatesRequest;
import io.kimconsulting.maxbots.api.request.SendMessageRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public final class MaxApiClient implements MaxApi, RawMaxApiClient {
    public static final URI DEFAULT_BASE_URI = URI.create("https://platform-api.max.ru");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final URI baseUri;
    private final String token;

    public MaxApiClient(String token) {
        this(builder(token));
    }

    private MaxApiClient(Builder builder) {
        this.token = Objects.requireNonNull(builder.token, "token");
        this.baseUri = Objects.requireNonNullElse(builder.baseUri, DEFAULT_BASE_URI);
        this.httpClient = Objects.requireNonNullElseGet(builder.httpClient, () -> HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build());
        this.objectMapper = Objects.requireNonNullElseGet(builder.objectMapper, MaxObjectMapperFactory::create);
    }

    public static Builder builder(String token) {
        return new Builder(token);
    }

    @Override
    public BotInfo getMe() {
        return read(get("me", Map.of()), BotInfo.class);
    }

    @Override
    public UpdatesPage getUpdates(GetUpdatesRequest request) {
        return read(get("updates", request.toQueryMap()), UpdatesPage.class);
    }

    @Override
    public Message sendMessage(SendMessageRequest request) {
        return read(post("messages", request.toQueryMap(), request.body()), Message.class);
    }

    @Override
    public Message editMessage(EditMessageRequest request) {
        JsonNode response = put("messages", Map.of("message_id", request.messageId()), request.body());
        JsonNode maybeMessage = response.get("message");
        if (maybeMessage != null && maybeMessage.isObject()) {
            return read(maybeMessage, Message.class);
        }
        return read(response, Message.class);
    }

    @Override
    public ActionResult deleteMessage(String messageId) {
        return read(delete("messages", Map.of("message_id", messageId)), ActionResult.class);
    }

    @Override
    public Message getMessage(String messageId) {
        return read(get("messages/" + urlEncode(messageId), Map.of()), Message.class);
    }

    @Override
    public List<Message> getMessages(GetMessagesRequest request) {
        MessagesResponse response = read(get("messages", request.toQueryMap()), MessagesResponse.class);
        return response.messages() == null ? List.of() : response.messages();
    }

    @Override
    public List<Subscription> getSubscriptions() {
        SubscriptionsResponse response = read(get("subscriptions", Map.of()), SubscriptionsResponse.class);
        return response.subscriptions() == null ? List.of() : response.subscriptions();
    }

    @Override
    public ActionResult createSubscription(CreateSubscriptionRequest request) {
        return read(post("subscriptions", Map.of(), request), ActionResult.class);
    }

    @Override
    public ActionResult deleteSubscription(String url) {
        return read(delete("subscriptions", Map.of("url", url)), ActionResult.class);
    }

    @Override
    public ActionResult answerCallback(AnswerCallbackRequest request) {
        return read(post("answers", Map.of("callback_id", request.callbackId()), request.toBody()), ActionResult.class);
    }

    @Override
    public RawMaxApiClient raw() {
        return this;
    }

    @Override
    public JsonNode get(String path, Map<String, ?> query) {
        return send("GET", path, query, null);
    }

    @Override
    public JsonNode post(String path, Map<String, ?> query, Object body) {
        return send("POST", path, query, body);
    }

    @Override
    public JsonNode put(String path, Map<String, ?> query, Object body) {
        return send("PUT", path, query, body);
    }

    @Override
    public JsonNode patch(String path, Map<String, ?> query, Object body) {
        return send("PATCH", path, query, body);
    }

    @Override
    public JsonNode delete(String path, Map<String, ?> query) {
        return send("DELETE", path, query, null);
    }

    public Message sendMessageToUser(long userId, String text) {
        return sendMessage(SendMessageRequest.toUser(userId, text));
    }

    public Message sendMessageToChat(long chatId, String text) {
        return sendMessage(SendMessageRequest.toChat(chatId, text));
    }

    public Message sendMessageToUser(long userId, io.kimconsulting.maxbots.api.model.NewMessageBody body) {
        return sendMessage(SendMessageRequest.toUser(userId, body));
    }

    public Message sendMessageToChat(long chatId, io.kimconsulting.maxbots.api.model.NewMessageBody body) {
        return sendMessage(SendMessageRequest.toChat(chatId, body));
    }

    public ObjectMapper objectMapper() {
        return objectMapper;
    }

    private JsonNode send(String method, String path, Map<String, ?> query, Object body) {
        try {
            HttpRequest request = buildRequest(method, path, query, body);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new MaxApiException("MAX API request failed with status " + response.statusCode(), response.statusCode(), response.body());
            }
            if (response.body() == null || response.body().isBlank()) {
                return objectMapper.nullNode();
            }
            return objectMapper.readTree(response.body());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new MaxApiException("Unable to call MAX API", ex);
        } catch (IOException ex) {
            throw new MaxApiException("Unable to call MAX API", ex);
        }
    }

    private HttpRequest buildRequest(String method, String path, Map<String, ?> query, Object body) throws JsonProcessingException {
        String relativePath = path.startsWith("/") ? path.substring(1) : path;
        URI uri = URI.create(baseUri.toString() + "/" + relativePath + toQueryString(query));
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
            .header("Authorization", token)
            .header("Accept", "application/json");

        if (body != null) {
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        return builder.build();
    }

    private String toQueryString(Map<String, ?> query) {
        if (query == null || query.isEmpty()) {
            return "";
        }
        List<String> params = new ArrayList<>();
        for (Map.Entry<String, ?> entry : query.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (entry.getValue() instanceof Iterable<?> iterable) {
                StringJoiner joiner = new StringJoiner(",");
                for (Object value : iterable) {
                    if (value != null) {
                        joiner.add(String.valueOf(value));
                    }
                }
                String joined = joiner.toString();
                if (!joined.isBlank()) {
                    params.add(urlEncode(entry.getKey()) + "=" + urlEncode(joined));
                }
                continue;
            }
            params.add(urlEncode(entry.getKey()) + "=" + urlEncode(String.valueOf(entry.getValue())));
        }
        return params.isEmpty() ? "" : "?" + String.join("&", params);
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private <T> T read(JsonNode node, Class<T> type) {
        return readNode(unpack(node), type);
    }

    private <T> T readNode(JsonNode node, Class<T> type) {
        try {
            return objectMapper.treeToValue(node, type);
        } catch (JsonProcessingException ex) {
            throw new MaxApiException("Unable to decode MAX API response into " + type.getSimpleName(), ex);
        }
    }

    private JsonNode unpack(JsonNode node) {
        if (node == null || node.isNull()) {
            return objectMapper.nullNode();
        }
        if (node.has("result")) {
            return node.get("result");
        }
        return node;
    }

    public static final class Builder {
        private final String token;
        private HttpClient httpClient;
        private ObjectMapper objectMapper;
        private URI baseUri;

        private Builder(String token) {
            this.token = token;
        }

        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public Builder baseUri(URI baseUri) {
            this.baseUri = baseUri;
            return this;
        }

        public MaxApiClient build() {
            return new MaxApiClient(this);
        }
    }
}
