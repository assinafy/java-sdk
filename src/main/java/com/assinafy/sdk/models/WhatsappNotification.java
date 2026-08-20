package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Rendered WhatsApp notification sent for an assignment. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsappNotification {

    @JsonProperty("sent_at")
    private Long sentAt;

    @JsonProperty("header")
    private String header;

    @JsonProperty("body")
    private String body;

    @JsonProperty("buttons")
    private List<Button> buttons;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("signer_id")
    private String signerId;

    /**
     * Creates an empty WhatsApp notification.
     */
    public WhatsappNotification() {}

    /**
     * Returns the send timestamp.
     *
     * @return the send timestamp
     */
    public Long getSentAt() { return sentAt; }

    /**
     * Sets the send timestamp.
     *
     * @param sentAt the send timestamp
     */
    public void setSentAt(Long sentAt) { this.sentAt = sentAt; }

    /**
     * Returns the header.
     *
     * @return the header
     */
    public String getHeader() { return header; }

    /**
     * Sets the header.
     *
     * @param header the header
     */
    public void setHeader(String header) { this.header = header; }

    /**
     * Returns the body.
     *
     * @return the body
     */
    public String getBody() { return body; }

    /**
     * Sets the body.
     *
     * @param body the body
     */
    public void setBody(String body) { this.body = body; }

    /**
     * Returns the buttons.
     *
     * @return the buttons
     */
    public List<Button> getButtons() { return buttons; }

    /**
     * Sets the buttons.
     *
     * @param buttons the buttons
     */
    public void setButtons(List<Button> buttons) { this.buttons = buttons; }

    /**
     * Returns the phone number.
     *
     * @return the phone number
     */
    public String getPhoneNumber() { return phoneNumber; }

    /**
     * Sets the phone number.
     *
     * @param phoneNumber the phone number
     */
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    /**
     * Returns the signer ID.
     *
     * @return the signer ID
     */
    public String getSignerId() { return signerId; }

    /**
     * Sets the signer ID.
     *
     * @param signerId the signer ID
     */
    public void setSignerId(String signerId) { this.signerId = signerId; }

    /** A button rendered under the WhatsApp notification body. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Button {

        @JsonProperty("text")
        private String text;

        /**
         * Creates an empty button.
         */
        public Button() {}

        /**
         * Returns the text.
         *
         * @return the text
         */
        public String getText() { return text; }

        /**
         * Sets the text.
         *
         * @param text the text
         */
        public void setText(String text) { this.text = text; }
    }
}
