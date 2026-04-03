package io.kimconsulting.maxbots.core;

import io.kimconsulting.maxbots.api.http.MaxApi;
import io.kimconsulting.maxbots.api.http.RawMaxApiClient;
import io.kimconsulting.maxbots.api.model.ActionResult;
import io.kimconsulting.maxbots.api.model.Callback;
import io.kimconsulting.maxbots.api.model.Message;
import io.kimconsulting.maxbots.api.model.MessageBody;
import io.kimconsulting.maxbots.api.model.Recipient;
import io.kimconsulting.maxbots.api.model.Subscription;
import io.kimconsulting.maxbots.api.model.Update;
import io.kimconsulting.maxbots.api.model.UpdateType;
import io.kimconsulting.maxbots.api.model.UpdatesPage;
import io.kimconsulting.maxbots.api.model.User;
import io.kimconsulting.maxbots.api.request.AnswerCallbackRequest;
import io.kimconsulting.maxbots.api.request.CreateSubscriptionRequest;
import io.kimconsulting.maxbots.api.request.EditMessageRequest;
import io.kimconsulting.maxbots.api.request.GetMessagesRequest;
import io.kimconsulting.maxbots.api.request.GetUpdatesRequest;
import io.kimconsulting.maxbots.api.request.SendMessageRequest;
import io.kimconsulting.maxbots.core.bot.MaxBot;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MaxBotTest {
    @Test
    void matchesCommandAndActionHandlers() {
        RecordingApi api = new RecordingApi();
        MaxBot bot = new MaxBot(api);
        AtomicBoolean commandHandled = new AtomicBoolean(false);
        AtomicBoolean actionHandled = new AtomicBoolean(false);

        bot.command("start", ctx -> {
            commandHandled.set(true);
            ctx.reply("ok");
        });
        bot.action("approve", ctx -> {
            actionHandled.set(true);
            ctx.answer("done");
        });

        bot.handle(new Update(
            UpdateType.MESSAGE_CREATED,
            1L,
            new Message(new User(7L, "Ivan", null, "ivan", false, null, null, null), new Recipient(null, 99L, "chat"), null, null,
                new MessageBody("m-1", 1L, "/start", List.of()), null, null),
            null,
            null,
            null,
            null
        ));

        bot.handle(new Update(
            UpdateType.MESSAGE_CALLBACK,
            2L,
            null,
            new Callback("cb-1", "approve", new User(7L, "Ivan", null, "ivan", false, null, null, null),
                new Message(new User(7L, "Ivan", null, "ivan", false, null, null, null), new Recipient(null, 99L, "chat"), null, null,
                    new MessageBody("m-2", 2L, "Click", List.of()), null, null)),
            null,
            null,
            null
        ));

        Assertions.assertTrue(commandHandled.get());
        Assertions.assertTrue(actionHandled.get());
        Assertions.assertEquals("ok", api.lastSentMessageText);
        Assertions.assertEquals("done", api.lastNotification);
    }

    private static final class RecordingApi implements MaxApi {
        private String lastSentMessageText;
        private String lastNotification;

        @Override
        public io.kimconsulting.maxbots.api.model.BotInfo getMe() {
            return null;
        }

        @Override
        public UpdatesPage getUpdates(GetUpdatesRequest request) {
            return null;
        }

        @Override
        public Message sendMessage(SendMessageRequest request) {
            lastSentMessageText = request.body().getText();
            return new Message(null, null, null, null, new MessageBody("sent", null, request.body().getText(), List.of()), null, null);
        }

        @Override
        public Message editMessage(EditMessageRequest request) {
            return null;
        }

        @Override
        public ActionResult deleteMessage(String messageId) {
            return null;
        }

        @Override
        public Message getMessage(String messageId) {
            return null;
        }

        @Override
        public List<Message> getMessages(GetMessagesRequest request) {
            return List.of();
        }

        @Override
        public List<Subscription> getSubscriptions() {
            return List.of();
        }

        @Override
        public ActionResult createSubscription(CreateSubscriptionRequest request) {
            return null;
        }

        @Override
        public ActionResult deleteSubscription(String url) {
            return null;
        }

        @Override
        public ActionResult answerCallback(AnswerCallbackRequest request) {
            lastNotification = request.notification();
            return new ActionResult(true, "ok");
        }

        @Override
        public RawMaxApiClient raw() {
            return null;
        }
    }
}
