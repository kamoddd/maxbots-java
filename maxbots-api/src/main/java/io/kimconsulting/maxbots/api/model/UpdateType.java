package io.kimconsulting.maxbots.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UpdateType {
    BOT_STARTED("bot_started"),
    MESSAGE_CREATED("message_created"),
    MESSAGE_REMOVED("message_removed"),
    MESSAGE_EDITED("message_edited"),
    BOT_ADDED("bot_added"),
    BOT_REMOVED("bot_removed"),
    USER_ADDED("user_added"),
    USER_REMOVED("user_removed"),
    CHAT_TITLE_CHANGED("chat_title_changed"),
    MESSAGE_CALLBACK("message_callback"),
    UNKNOWN("unknown");

    private final String value;

    UpdateType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static UpdateType fromValue(String value) {
        for (UpdateType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
