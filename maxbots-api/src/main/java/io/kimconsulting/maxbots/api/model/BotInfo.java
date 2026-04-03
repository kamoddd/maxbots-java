package io.kimconsulting.maxbots.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BotInfo(
    Long userId,
    String firstName,
    String lastName,
    String username,
    Boolean isBot,
    Long lastActivityTime,
    String description,
    String avatarUrl,
    List<BotCommand> commands
) {
}
