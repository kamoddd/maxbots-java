package io.kimconsulting.maxbots.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record KeyboardButton(
    String type,
    String text,
    String payload,
    String url,
    String intent,
    Boolean quick,
    String webApp,
    Long contactId
) {
    public static KeyboardButton callback(String text, String payload) {
        return new KeyboardButton("callback", text, payload, null, null, null, null, null);
    }

    public static KeyboardButton callback(String text, String payload, String intent) {
        return new KeyboardButton("callback", text, payload, null, intent, null, null, null);
    }

    public static KeyboardButton link(String text, String url) {
        return new KeyboardButton("link", text, null, url, null, null, null, null);
    }

    public static KeyboardButton message(String text) {
        return new KeyboardButton("message", text, null, null, null, null, null, null);
    }

    public static KeyboardButton requestContact(String text) {
        return new KeyboardButton("request_contact", text, null, null, null, null, null, null);
    }

    public static KeyboardButton requestGeoLocation(String text, boolean quick) {
        return new KeyboardButton("request_geo_location", text, null, null, null, quick, null, null);
    }

    public static KeyboardButton openApp(String text, String webApp, Long contactId) {
        return new KeyboardButton("open_app", text, null, null, null, null, webApp, contactId);
    }
}
