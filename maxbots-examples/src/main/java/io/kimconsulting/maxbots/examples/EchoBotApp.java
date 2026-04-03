package io.kimconsulting.maxbots.examples;

import io.kimconsulting.maxbots.api.model.Attachment;
import io.kimconsulting.maxbots.api.model.AttachmentRequest;
import io.kimconsulting.maxbots.api.model.BotInfo;
import io.kimconsulting.maxbots.api.model.InlineKeyboardAttachment;
import io.kimconsulting.maxbots.api.model.KeyboardButton;
import io.kimconsulting.maxbots.api.model.UpdateType;
import io.kimconsulting.maxbots.core.bot.MaxBot;
import io.kimconsulting.maxbots.core.polling.PollingOptions;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EchoBotApp {
    private static final Logger log = LoggerFactory.getLogger(EchoBotApp.class);
    private static final int DEDUP_CACHE_SIZE = 1_000;
    private static final Map<String, Boolean> SEEN_UPDATES = java.util.Collections.synchronizedMap(
        new LinkedHashMap<>(DEDUP_CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                return size() > DEDUP_CACHE_SIZE;
            }
        }
    );

    private EchoBotApp() {
    }

    public static void main(String[] args) {
        BotConfiguration configuration = BotConfiguration.load();
        BotInstanceLock instanceLock = BotInstanceLock.acquire(Path.of("echo-bot.lock"));

        MaxBot bot = MaxBot.withToken(configuration.token());
        BotInfo me = bot.api().getMe();
        Long botUserId = me != null ? me.userId() : null;
        String botUsername = me != null ? me.username() : null;

        bot.use((ctx, next) -> {
            debug(configuration, "Incoming update: type={} mid={} callbackId={} userId={} text={} attachments={}",
                ctx.updateType(),
                messageId(ctx),
                callbackId(ctx),
                ctx.userId(),
                sanitize(ctx.text()),
                summarizeAttachments(ctx.attachments()));
            if (isDuplicate(ctx)) {
                debug(configuration, "Skipped duplicate update: type={} mid={} callbackId={}",
                    ctx.updateType(),
                    messageId(ctx),
                    callbackId(ctx));
                return;
            }
            next.proceed(ctx);
        });

        bot.command("start", ctx -> ctx.reply(
            """
            Привет! Я demo echo bot для MAX.
            Отправьте мне текст, картинку или другое вложение, и я отвечу.
            Ниже есть demo-кнопки для callback-обработчиков.
            """,
            demoKeyboard(configuration)
        ));

        bot.command("menu", ctx -> ctx.reply(
            "Меню бота:",
            demoKeyboard(configuration)
        ));

        bot.action("demo:whoami", ctx -> {
            String name = ctx.message() != null && ctx.message().sender() != null
                ? ctx.message().sender().displayName()
                : "неизвестный пользователь";
            ctx.answer("Показываю информацию о вас");
            debug(configuration, "Answering callback demo:whoami for userId={}", ctx.userId());
            ctx.reply("Вы: " + name + " | userId=" + ctx.userId());
            debug(configuration, "Sent whoami reply for mid={}", messageId(ctx));
        });

        bot.action("demo:help", ctx -> {
            ctx.answer("Показываю подсказку");
            debug(configuration, "Answering callback demo:help for userId={}", ctx.userId());
            ctx.reply(
                """
                Команды:
                /start - приветствие
                /menu - показать кнопки

                Что можно проверить:
                - текстовый echo
                - картинки и другие вложения
                - callback buttons
                """,
                demoKeyboard(configuration)
            );
            debug(configuration, "Sent help reply for mid={}", messageId(ctx));
        });

        bot.action("demo:about", ctx -> {
            ctx.answer("Показываю информацию о боте");
            debug(configuration, "Answering callback demo:about for userId={}", ctx.userId());
            ctx.reply(
                """
                Этот бот демонстрирует:
                - long polling
                - echo reply для текста
                - ответ на картинки и другие вложения
                - callback buttons
                """,
                demoKeyboard(configuration)
            );
            debug(configuration, "Sent about reply for mid={}", messageId(ctx));
        });

        bot.on(UpdateType.MESSAGE_CREATED, ctx -> {
            if (isOwnMessage(ctx, botUserId, botUsername)) {
                debug(configuration, "Ignored own/bot message: mid={}", messageId(ctx));
                return;
            }

            String text = ctx.text();
            if (text != null && (text.startsWith("/start") || text.startsWith("/menu"))) {
                debug(configuration, "Ignored command in generic echo handler: mid={} text={}", messageId(ctx), sanitize(text));
                return;
            }

            List<Attachment> attachments = ctx.attachments();
            if (text != null && !text.isBlank()) {
                String attachmentSummary = summarizeAttachments(attachments);
                if (attachmentSummary == null) {
                    debug(configuration, "Sending text echo for mid={} text={}", messageId(ctx), sanitize(text));
                    ctx.replyToCurrentMessage("echo: " + text);
                } else {
                    debug(configuration, "Sending text+attachment echo for mid={} text={} attachments={}",
                        messageId(ctx), sanitize(text), attachmentSummary);
                    ctx.replyToCurrentMessage("echo: " + text + "\nВложения: " + attachmentSummary);
                }
                return;
            }

            if (!attachments.isEmpty()) {
                debug(configuration, "Sending attachment-only reply for mid={} attachments={}",
                    messageId(ctx), summarizeAttachments(attachments));
                ctx.replyToCurrentMessage("Получил вложение: " + summarizeAttachments(attachments));
            }
        });

        bot.polling(PollingOptions.builder()
            .updateTypes(List.of(UpdateType.MESSAGE_CREATED, UpdateType.MESSAGE_CALLBACK))
            .build());

        System.out.println("Echo bot started as @" + (me != null ? me.username() : configuration.botName())
            + " pid=" + ProcessHandle.current().pid());
        bot.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            bot.stop();
            instanceLock.close();
        }));

        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            bot.stop();
        } finally {
            instanceLock.close();
        }
    }

    private static AttachmentRequest demoKeyboard(BotConfiguration configuration) {
        return AttachmentRequest.inlineKeyboard(
            InlineKeyboardAttachment.builder()
                .row(
                    KeyboardButton.callback("Who Am I", "demo:whoami"),
                    KeyboardButton.callback("Help", "demo:help"),
                    KeyboardButton.callback("About", "demo:about")
                )
                .build()
        );
    }

    private static boolean isOwnMessage(io.kimconsulting.maxbots.core.context.MaxContext ctx, Long botUserId, String botUsername) {
        if (botUserId != null && botUserId.equals(ctx.userId())) {
            return true;
        }
        if (ctx.message() != null && ctx.message().sender() != null) {
            if (Boolean.TRUE.equals(ctx.message().sender().isBot())) {
                return true;
            }
            if (botUsername != null && botUsername.equalsIgnoreCase(ctx.message().sender().username())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDuplicate(io.kimconsulting.maxbots.core.context.MaxContext ctx) {
        String key = null;
        if (ctx.callback() != null && ctx.callback().callbackId() != null) {
            key = "callback:" + ctx.callback().callbackId();
        } else if (ctx.message() != null && ctx.message().body() != null && ctx.message().body().mid() != null) {
            key = "message:" + ctx.message().body().mid();
        }

        if (key == null) {
            return false;
        }

        synchronized (SEEN_UPDATES) {
            if (SEEN_UPDATES.containsKey(key)) {
                return true;
            }
            SEEN_UPDATES.put(key, Boolean.TRUE);
            return false;
        }
    }

    private static String summarizeAttachments(List<Attachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return null;
        }
        List<String> types = new ArrayList<>();
        for (Attachment attachment : attachments) {
            if (attachment != null && attachment.type() != null && !attachment.type().isBlank()) {
                types.add(attachment.type());
            }
        }
        return types.isEmpty() ? "unknown attachment" : String.join(", ", types);
    }

    private static String messageId(io.kimconsulting.maxbots.core.context.MaxContext ctx) {
        if (ctx.message() == null || ctx.message().body() == null) {
            return null;
        }
        return ctx.message().body().mid();
    }

    private static String callbackId(io.kimconsulting.maxbots.core.context.MaxContext ctx) {
        return ctx.callback() != null ? ctx.callback().callbackId() : null;
    }

    private static String sanitize(String value) {
        if (value == null) {
            return null;
        }
        return value.replace('\n', ' ').trim();
    }

    private static void debug(BotConfiguration configuration, String message, Object... args) {
        if (configuration.debug()) {
            log.info(message, args);
        }
    }
}
