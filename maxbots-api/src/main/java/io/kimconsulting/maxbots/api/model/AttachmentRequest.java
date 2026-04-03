package io.kimconsulting.maxbots.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AttachmentRequest(
    String type,
    Object payload
) {
    public static AttachmentRequest of(String type, Object payload) {
        return new AttachmentRequest(type, payload);
    }

    public static AttachmentRequest inlineKeyboard(InlineKeyboardAttachment keyboard) {
        return of("inline_keyboard", keyboard);
    }

    public static AttachmentRequest imageToken(String token) {
        return of("image", new TokenPayload(token));
    }

    public static AttachmentRequest videoToken(String token) {
        return of("video", new TokenPayload(token));
    }

    public static AttachmentRequest audioToken(String token) {
        return of("audio", new TokenPayload(token));
    }

    public static AttachmentRequest fileToken(String token) {
        return of("file", new TokenPayload(token));
    }

    public static AttachmentRequest sticker(String code) {
        return of("sticker", new StickerPayload(code));
    }

    public static AttachmentRequest location(double latitude, double longitude) {
        return of("location", new LocationPayload(latitude, longitude));
    }
}
