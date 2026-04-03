package io.kimconsulting.maxbots.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class InlineKeyboardAttachment {
    private final List<List<KeyboardButton>> buttons;

    public InlineKeyboardAttachment(List<List<KeyboardButton>> buttons) {
        this.buttons = List.copyOf(buttons);
    }

    public static InlineKeyboardAttachment ofRow(KeyboardButton... buttons) {
        return new Builder().row(buttons).build();
    }

    public List<List<KeyboardButton>> getButtons() {
        return buttons;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<List<KeyboardButton>> rows = new ArrayList<>();

        public Builder row(KeyboardButton... buttons) {
            List<KeyboardButton> row = new ArrayList<>();
            for (KeyboardButton button : buttons) {
                row.add(Objects.requireNonNull(button, "button"));
            }
            rows.add(List.copyOf(row));
            return this;
        }

        public InlineKeyboardAttachment build() {
            return new InlineKeyboardAttachment(rows);
        }
    }
}
