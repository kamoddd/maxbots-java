package io.kimconsulting.maxbots.webhook;

import io.kimconsulting.maxbots.core.bot.MaxBot;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MaxWebhookHandlerTest {
    @Test
    void rejectsInvalidSecret() {
        MaxWebhookHandler handler = new MaxWebhookHandler("expected-secret");
        Assertions.assertThrows(MaxWebhookException.class, () -> handler.verifySecret("wrong"));
    }

    @Test
    void parsesAndDispatchesWebhookBody() {
        AtomicBoolean seen = new AtomicBoolean(false);
        MaxBot bot = MaxBot.withToken("test-token");
        bot.onAny(ctx -> seen.set(true));

        MaxWebhookHandler handler = new MaxWebhookHandler("expected-secret");
        handler.handle("""
            {
              "update_type": "message_created",
              "message": {
                "recipient": {
                  "chat_id": 55
                },
                "body": {
                  "mid": "m-1",
                  "text": "hi"
                }
              }
            }
            """, "expected-secret", bot);

        Assertions.assertTrue(seen.get());
    }
}
