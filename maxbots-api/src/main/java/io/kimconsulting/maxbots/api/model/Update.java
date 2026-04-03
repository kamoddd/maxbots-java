package io.kimconsulting.maxbots.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Update(
    UpdateType updateType,
    Long timestamp,
    Message message,
    Callback callback,
    User user,
    Chat chat,
    String userLocale
) {
}
