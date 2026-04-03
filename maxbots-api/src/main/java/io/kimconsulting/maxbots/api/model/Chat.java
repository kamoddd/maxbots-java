package io.kimconsulting.maxbots.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Chat(
    Long chatId,
    String type,
    String status,
    String title,
    String iconUrl,
    String description
) {
}
