# MAX Bots Java

[![JitPack](https://jitpack.io/v/kamoddd/maxbots-java.svg)](https://jitpack.io/#kamoddd/maxbots-java)

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

For local development from source the internal coordinates are:

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

## Install via JitPack

JitPack builds are created from Git tags and served as Maven artifacts. JitPack's official docs say:
- push a GitHub release/tag and JitPack will build it from source
- for multi-module projects, use `com.github.User.Repo` as the group id and module artifact ids individually

Sources:
- [JitPack intro](https://docs.jitpack.io/)
- [JitPack multi-module docs](https://docs.jitpack.io/building/)

Add JitPack to Maven:

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

Then depend on a released tag, for example `v0.1.1`.

Maven:

```xml
<dependency>
  <groupId>com.github.kamoddd.maxbots-java</groupId>
  <artifactId>maxbots-api</artifactId>
  <version>v0.1.1</version>
</dependency>
```

```xml
<dependency>
  <groupId>com.github.kamoddd.maxbots-java</groupId>
  <artifactId>maxbots-core</artifactId>
  <version>v0.1.1</version>
</dependency>
```

```xml
<dependency>
  <groupId>com.github.kamoddd.maxbots-java</groupId>
  <artifactId>maxbots-webhook</artifactId>
  <version>v0.1.1</version>
</dependency>
```

Direct artifact examples:

- `https://jitpack.io/com/github/kamoddd/maxbots-java/maxbots-core/v0.1.1/maxbots-core-v0.1.1.jar`
- `https://jitpack.io/com/github/kamoddd/maxbots-java/maxbots-core/v0.1.1/maxbots-core-v0.1.1.pom`

Gradle:

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.kamoddd.maxbots-java:maxbots-core:v0.1.1'
}
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
./mvnw -q -Dmaven.repo.local=.m2 -pl maxbots-examples -am package
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
