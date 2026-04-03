package io.kimconsulting.maxbots.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record User(
    Long userId,
    String firstName,
    String lastName,
    String username,
    Boolean isBot,
    Long lastActivityTime,
    String avatarUrl,
    String description
) {
    public String displayName() {
        if (lastName == null || lastName.isBlank()) {
            return firstName;
        }
        return firstName + " " + lastName;
    }
}
