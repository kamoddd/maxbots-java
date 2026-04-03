package io.kimconsulting.maxbots.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Message(
    User sender,
    Recipient recipient,
    Long timestamp,
    LinkedMessage link,
    MessageBody body,
    MessageStat stat,
    String url
) {
}
