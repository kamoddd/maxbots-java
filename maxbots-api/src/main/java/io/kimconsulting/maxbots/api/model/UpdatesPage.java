package io.kimconsulting.maxbots.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdatesPage(
    List<Update> updates,
    Long marker
) {
}
