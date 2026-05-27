package com.assinafy.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Body for {@code POST /accounts/{account_id}/tags} (Create Tag).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateTagRequest {

    /** Tag name (required). */
    @JsonProperty("name")
    private String name;

    /** Optional six-character hex colour without a leading {@code #} (e.g. {@code FF0000}). */
    @JsonProperty("color")
    private String color;

    public CreateTagRequest() {}

    public CreateTagRequest(String name) { this.name = name; }

    public static Builder builder() { return new Builder(); }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public static final class Builder {
        private final CreateTagRequest req = new CreateTagRequest();

        public Builder name(String name) { req.setName(name); return this; }
        public Builder color(String color) { req.setColor(color); return this; }
        public CreateTagRequest build() { return req; }
    }
}
