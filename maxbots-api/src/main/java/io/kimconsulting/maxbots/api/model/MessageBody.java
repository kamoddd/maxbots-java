package io.kimconsulting.maxbots.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MessageBody(
    String mid,
    Long seq,
    String text,
    List<Attachment> attachments
) {
}
