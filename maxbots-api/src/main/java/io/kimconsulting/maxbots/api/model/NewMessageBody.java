package io.kimconsulting.maxbots.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class NewMessageBody {
    private final String text;
    private final List<AttachmentRequest> attachments;
    private final LinkedMessage link;
    private final Boolean notify;
    private final TextFormat format;

    public NewMessageBody(String text, List<AttachmentRequest> attachments, LinkedMessage link, Boolean notify, TextFormat format) {
        this.text = text;
        this.attachments = attachments == null ? null : List.copyOf(attachments);
        this.link = link;
        this.notify = notify;
        this.format = format;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static NewMessageBody text(String text) {
        return builder().text(text).build();
    }

    public String getText() {
        return text;
    }

    public List<AttachmentRequest> getAttachments() {
        return attachments;
    }

    public LinkedMessage getLink() {
        return link;
    }

    public Boolean getNotify() {
        return notify;
    }

    public TextFormat getFormat() {
        return format;
    }

    public static final class Builder {
        private String text;
        private final List<AttachmentRequest> attachments = new ArrayList<>();
        private LinkedMessage link;
        private Boolean notify;
        private TextFormat format;

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder attachment(AttachmentRequest attachment) {
            this.attachments.add(attachment);
            return this;
        }

        public Builder attachments(List<AttachmentRequest> attachments) {
            this.attachments.clear();
            this.attachments.addAll(attachments);
            return this;
        }

        public Builder link(LinkedMessage link) {
            this.link = link;
            return this;
        }

        public Builder notify(boolean notify) {
            this.notify = notify;
            return this;
        }

        public Builder format(TextFormat format) {
            this.format = format;
            return this;
        }

        public NewMessageBody build() {
            return new NewMessageBody(text, attachments.isEmpty() ? null : attachments, link, notify, format);
        }
    }
}
