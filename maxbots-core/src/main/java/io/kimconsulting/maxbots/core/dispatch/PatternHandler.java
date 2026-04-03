package io.kimconsulting.maxbots.core.dispatch;

import io.kimconsulting.maxbots.core.context.ContextHandler;
import java.util.regex.Pattern;

public record PatternHandler(
    Pattern pattern,
    ContextHandler handler
) {
}
