package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Account branding theme, as returned by {@code GET /accounts/{accountId}/theme}: the display
 * name, primary/secondary brand colours (6-char hex) and the logo URL ({@code null} when no logo
 * has been uploaded).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountTheme {

    @JsonProperty("account_name")
    private String accountName;

    @JsonProperty("primary_color")
    private String primaryColor;

    @JsonProperty("secondary_color")
    private String secondaryColor;

    @JsonProperty("logo")
    private String logo;

    public AccountTheme() {}

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }

    public String getSecondaryColor() { return secondaryColor; }
    public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
}
