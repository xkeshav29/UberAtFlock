package com.uberforflock.service;

import com.uberforflock.dao.OauthCredentialDao;
import com.uberforflock.model.OauthCredential;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by kumarke on 8/23/16.
 */

@Service
public class UserAuthService {

    @Autowired
    private OauthCredentialDao oauthCredentialDao;

    @Autowired
    private Oauth2TokenService oauth2TokenService;

    private static final int EXPIRY_TOLERANCE_IN_MIN = 5;

    private static final Logger logger = LoggerFactory.getLogger(UserAuthService.class);

    public String getAccessToken(String userid) {
        OauthCredential credential = oauthCredentialDao.getCredential(userid);
        if(credential.isExpired(EXPIRY_TOLERANCE_IN_MIN) && credential.getRefreshToken()!= null) {
            logger.info("Refreshing access token for user:{}", userid);
            credential = refreshAccessToken(credential.getRefreshToken());
            if(credential != null)
                oauthCredentialDao.setRefreshedCredential(userid, credential.getAccessToken(), credential.getExpiry());
        }
        return credential != null ? credential.getAccessToken() : null;
    }

    private OauthCredential refreshAccessToken(String refreshToken) {
        try {
            return oauth2TokenService.getOauth2Credential(refreshToken, GrantType.REFRESH_TOKEN);
        }catch (Exception e){
            logger.error("Failed refreshing token {}" , refreshToken, e);
            return null;
        }
    }
}
