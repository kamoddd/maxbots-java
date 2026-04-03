# MAX Bots Java

`MAX Bots Java` is a modern Java library for building bots on top of the [MAX Bot API](https://dev.max.ru/docs-api).

The project is intentionally layered:

- `maxbots-api`: low-level typed HTTP client plus raw API access
- `maxbots-core`: bot DSL, middleware, routing, command/action handlers, long polling
- `maxbots-webhook`: webhook parsing and secret validation utilities

## Why this project

The official MAX ecosystem already documents the HTTP API and provides Go/JavaScript libraries. This project focuses on the Java experience:

- pleasant bot-first API similar to `command`, `hears`, `action`, `on`
- direct access to raw endpoints when MAX adds new features
- framework-agnostic webhook handling
- simple Maven setup and Java 17 baseline

## Modules

### `maxbots-api`

```xml
<dependency>
  <groupId>io.kimconsulting</groupId>
  <artifactId>maxbots-api</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### `maxbots-core`

```xml
<dependency>
  <groupId>io.kimconsulting</groupId>
  <artifactId>maxbots-core</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### `maxbots-webhook`

```xml
<dependency>
  <groupId>io.kimconsulting</groupId>
  <artifactId>maxbots-webhook</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

```java
import io.kimconsulting.maxbots.api.model.AttachmentRequest;
import io.kimconsulting.maxbots.api.model.InlineKeyboardAttachment;
import io.kimconsulting.maxbots.api.model.KeyboardButton;
import io.kimconsulting.maxbots.core.bot.MaxBot;

public class EchoBot {
    public static void main(String[] args) {
        MaxBot bot = MaxBot.withToken(System.getenv("MAX_BOT_TOKEN"));

        bot.command("start", ctx -> ctx.reply(
            "Добро пожаловать в MAX!",
            AttachmentRequest.inlineKeyboard(
                InlineKeyboardAttachment.ofRow(
                    KeyboardButton.callback("Ping", "ping"),
                    KeyboardButton.link("MAX", "https://max.ru")
                )
            )
        ));

        bot.hears("ping", ctx -> ctx.reply("pong"));
        bot.action("ping", ctx -> ctx.answer("Кнопка нажата"));

        bot.start();
    }
}
```

## Echo Bot Example

A runnable echo bot example is available in:

- `maxbots-examples/src/main/java/io/kimconsulting/maxbots/examples/EchoBotApp.java`
- `maxbots-examples/config/bot.properties.example`
- `maxbots-examples/config/bot.local.properties` (local only, gitignored)

What the demo shows:

- `/start` and `/menu`
- echo reply for any plain text message
- callback buttons
- reply to images and other attachments

Before the first run:

```bash
cp maxbots-examples/config/bot.properties.example maxbots-examples/config/bot.local.properties
```

Then put the real token into `maxbots-examples/config/bot.local.properties`.
That file is ignored by git and should never be committed.
Verbose debug logging can be enabled with `max.bot.debug=true`.

Run it from the project root:

```bash
./run-echo-bot.sh
```

You can also run it directly from the example module:

```bash
mvn -q -Dmaven.repo.local=.m2 -pl maxbots-examples -am package
java -jar maxbots-examples/target/maxbots-examples-0.1.0-SNAPSHOT.jar
```

## Design Notes

- The transport client targets `https://platform-api.max.ru`.
- Authentication uses the `Authorization` header exactly as described in the official docs.
- Long polling is intended for local development and testing.
- Webhook handling validates `X-Max-Bot-Api-Secret` when configured.
- When MAX ships a new endpoint, you can call it immediately through `api.raw()`.

## Status

First release scope:

- bot info
- send/edit/delete/get messages
- list messages
- long polling updates
- webhook subscription helpers
- callback answers
- inline keyboard builder

File upload helpers and broader chat management can be added in the next iteration on top of the same API foundation.
