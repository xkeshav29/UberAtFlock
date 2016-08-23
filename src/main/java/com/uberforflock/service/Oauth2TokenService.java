package com.uberforflock.service;

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.uberforflock.model.OauthCredential;
import com.uberforflock.util.Constants;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.springframework.stereotype.Service;

/**
 * Created by kumarke on 8/23/16.
 */
@Service
public class Oauth2TokenService {

    public OauthCredential getOauth2Credential( String code, GrantType grantType) throws Exception {
        String displayName = null;
        if (grantType.equals(GrantType.AUTHORIZATION_CODE)) {
            TokenResponse tokenResponse = new AuthorizationCodeTokenRequest(new NetHttpTransport(), new JacksonFactory(),
                    new GenericUrl(Constants.TOKEN_URL), code)
                    .set("client_id", Constants.CLIENT_ID)
                    .set("client_secret", Constants.CLIENT_SECRET)
                    .setGrantType(grantType.toString())
                    .setRedirectUri(Constants.OAUTH_CALLBACK_URL)
                    .execute();
            return new OauthCredential("dummy", tokenResponse.getAccessToken(), tokenResponse.getRefreshToken(), tokenResponse.getExpiresInSeconds());
        }
        if (grantType.equals(GrantType.REFRESH_TOKEN)) {
            TokenResponse tokenResponse = new RefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(),
                    new GenericUrl(Constants.TOKEN_URL), code)
                    .set("client_id", Constants.CLIENT_ID)
                    .set("client_secret", Constants.CLIENT_SECRET)
                    .execute();
            return new OauthCredential(displayName, tokenResponse.getAccessToken(), tokenResponse.getExpiresInSeconds());
        }
        return null;
    }
}
