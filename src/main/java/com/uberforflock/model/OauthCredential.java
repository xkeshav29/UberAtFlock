package com.uberforflock.model;

/**
 * Created by kumarke on 8/23/16.
 */
public class OauthCredential {
    private String accessToken;
    private String refreshToken;
    private Long expiry;
    private String displayName;


    public OauthCredential(String accessToken, String displayName) {
        this.displayName = displayName;
        this.accessToken = accessToken;
    }

    public OauthCredential(String displayName, String accessToken, Long expiresIn) {
        this.displayName = displayName;
        this.accessToken = accessToken;
        this.expiry = expiresIn;
    }

    public OauthCredential(String displayName, String accessToken, String refreshToken, Long expiresIn) {
        this.displayName = displayName;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiry = expiresIn;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpiry() {
        return expiry;
    }

    public void setExpiry(Long expiry) {
        this.expiry = expiry;
    }

    public boolean isExpired(int toleranceInMinutes) {
        //long running tokens never expire
        if (accessToken != null && expiry == null)
            return false;
        if (expiry == 0L)
            return false;
        //oauth2token about to expire in 'tolerance' minutes
        return expiry - (toleranceInMinutes * 60 * 1000) < System.currentTimeMillis();
    }
}
