package io.kimconsulting.maxbots.core.context;

import io.kimconsulting.maxbots.api.http.MaxApi;
import io.kimconsulting.maxbots.api.model.Attachment;
import io.kimconsulting.maxbots.api.model.AttachmentRequest;
import io.kimconsulting.maxbots.api.model.Callback;
import io.kimconsulting.maxbots.api.model.LinkedMessage;
import io.kimconsulting.maxbots.api.model.Message;
import io.kimconsulting.maxbots.api.model.NewMessageBody;
import io.kimconsulting.maxbots.api.model.Update;
import io.kimconsulting.maxbots.api.model.UpdateType;
import io.kimconsulting.maxbots.api.request.AnswerCallbackRequest;
import io.kimconsulting.maxbots.api.request.EditMessageRequest;
import io.kimconsulting.maxbots.api.request.SendMessageRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

public final class MaxContext {
    private final MaxApi api;
    private final Update update;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private Matcher matcher;

    public MaxContext(MaxApi api, Update update) {
        this.api = api;
        this.update = update;
    }

    public MaxApi api() {
        return api;
    }

    public Update update() {
        return update;
    }

    public UpdateType updateType() {
        return update.updateType();
    }

    public Message message() {
        if (update.message() != null) {
            return update.message();
        }
        if (update.callback() != null) {
            return update.callback().message();
        }
        return null;
    }

    public Callback callback() {
        return update.callback();
    }

    public Long chatId() {
        Message message = message();
        return message != null && message.recipient() != null ? message.recipient().chatId() : null;
    }

    public Long userId() {
        Message message = message();
        if (message != null && message.sender() != null && message.sender().userId() != null) {
            return message.sender().userId();
        }
        if (update.user() != null) {
            return update.user().userId();
        }
        return update.callback() != null && update.callback().user() != null ? update.callback().user().userId() : null;
    }

    public String text() {
        Message message = message();
        if (message == null || message.body() == null) {
            return null;
        }
        return message.body().text();
    }

    public Optional<Matcher> matcher() {
        return Optional.ofNullable(matcher);
    }

    public void matcher(Matcher matcher) {
        this.matcher = matcher;
    }

    public Object attribute(String key) {
        return attributes.get(key);
    }

    public <T> T attribute(String key, Class<T> type) {
        return type.cast(attributes.get(key));
    }

    public void attribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Message reply(String text) {
        return reply(NewMessageBody.text(text));
    }

    public Message reply(String text, AttachmentRequest... attachments) {
        NewMessageBody.Builder builder = NewMessageBody.builder().text(text);
        for (AttachmentRequest attachment : attachments) {
            builder.attachment(attachment);
        }
        return reply(builder.build());
    }

    public Message reply(NewMessageBody body) {
        if (chatId() != null) {
            return api.sendMessage(SendMessageRequest.toChat(chatId(), body));
        }
        if (userId() != null) {
            return api.sendMessage(SendMessageRequest.toUser(userId(), body));
        }
        throw new IllegalStateException("Cannot infer chat or user from update");
    }

    public Message replyToCurrentMessage(String text) {
        Message current = message();
        if (current == null || current.body() == null || current.body().mid() == null) {
            return reply(text);
        }
        return reply(NewMessageBody.builder()
            .text(text)
            .link(new LinkedMessage("reply", current.body().mid()))
            .build());
    }

    public Message editCurrentMessage(String text) {
        return editCurrentMessage(NewMessageBody.text(text));
    }

    public Message editCurrentMessage(NewMessageBody body) {
        Message current = message();
        if (current == null || current.body() == null || current.body().mid() == null) {
            throw new IllegalStateException("Current update does not point to a message");
        }
        return api.editMessage(new EditMessageRequest(current.body().mid(), body));
    }

    public void answer(String notification) {
        Callback callback = callback();
        if (callback == null || callback.callbackId() == null) {
            throw new IllegalStateException("Current update is not a callback");
        }
        api.answerCallback(AnswerCallbackRequest.notification(callback.callbackId(), notification));
    }

    public void answer(NewMessageBody body) {
        Callback callback = callback();
        if (callback == null || callback.callbackId() == null) {
            throw new IllegalStateException("Current update is not a callback");
        }
        api.answerCallback(AnswerCallbackRequest.message(callback.callbackId(), body));
    }

    public List<Attachment> attachments() {
        Message message = message();
        if (message == null || message.body() == null || message.body().attachments() == null) {
            return List.of();
        }
        return message.body().attachments();
    }
}
