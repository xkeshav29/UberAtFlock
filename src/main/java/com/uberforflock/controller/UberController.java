package com.uberforflock.controller;


import com.google.gson.Gson;
import com.uberforflock.dao.OauthCredentialDao;
import com.uberforflock.model.Availability;
import com.uberforflock.model.OauthCredential;
import com.uberforflock.service.CsrfTokenService;
import com.uberforflock.service.Oauth2TokenService;
import com.uberforflock.util.Constants;
import com.uberforflock.util.Util;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by kumarke on 8/22/16.
 */
@RestController
public class UberController {

    private static final Logger logger = LoggerFactory.getLogger(UberController.class);

    @Autowired
    private CsrfTokenService csrfTokenService;

    @Autowired
    private Oauth2TokenService oauthtokenService;

    @Autowired
    private OauthCredentialDao oauthCredentialDao;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value = "/authUrl", method = RequestMethod.GET)
    public String getAuthUrl(@RequestParam String userid){
        try {
            OAuthClientRequest.AuthenticationRequestBuilder requestBuilder = OAuthClientRequest
                    .authorizationLocation("https://login.uber.com/oauth/v2/authorize")
                    .setClientId("oZz5Gb_QSSe58a58C1hak9pURrnoYiU6")
                    .setRedirectURI("http://localhost:7011/callback")
                    .setState(getState(userid));
            Map<String, String> customParams = Collections.singletonMap("response_type","code");
            if (customParams != null && !customParams.isEmpty())
                customParams.forEach((key, value) -> requestBuilder.setParameter(key, value));
            OAuthClientRequest request = requestBuilder.buildQueryMessage();
            return request.getLocationUri().replaceAll(Pattern.quote("+"), "%20");
        } catch (Exception e) {
            logger.error("Could not get auth url for user:{}", userid, e);
            return "";
        }
    }

    @RequestMapping(value = "/callback", method = RequestMethod.GET)
    public void callbackHandler(@RequestParam Map<String, String> params, HttpServletResponse response){
        List<NameValuePair> qparams = new ArrayList<>();
        try {
            String code = params.get("code");
            String state = params.get("state");
            Map<String, String> stateMap = Util.parseJsonStringToMap(state);
            String user = stateMap.get("userid");
            String flockValidationToken = stateMap.get("flockValidationToken");
            String csrf_token = stateMap.get("csrf_token");
            qparams.add(new BasicNameValuePair("flockValidationToken", flockValidationToken));
            if (code == null || code.isEmpty()) {
                logger.info("Auth code not found for user {}", user);
                qparams.add(new BasicNameValuePair("status", "failure"));
                Util.redirect(response, Constants.FLOCK_REDIRECT_URL, qparams);
                return;
            }

            if (!csrfTokenService.isValidToken(csrf_token, user)) {
                logger.info("Invalid csrf Token " + csrf_token + " for the user " + user);
                qparams.add(new BasicNameValuePair("status", "failure"));
                Util.redirect(response, Constants.FLOCK_REDIRECT_URL, qparams);
                return;
            }
            OauthCredential credential = oauthtokenService.getOauth2Credential( code, GrantType.AUTHORIZATION_CODE);
            credential.setDisplayName("dummy");
            oauthCredentialDao.setCredential(stateMap.get("userid"), credential);
            logger.info("Saved credential for user {} for app {}", user);
            logger.info("Redirecting to:" + Constants.FLOCK_REDIRECT_URL);
            String uriParams = URLEncodedUtils.format(qparams, "UTF-8");
            String redirectUri = Constants.FLOCK_REDIRECT_URL + "?" + uriParams;
            response.sendRedirect(redirectUri);
        } catch (Exception e) {
            logger.error("Authentication failed", e);
            qparams.add(new BasicNameValuePair("status", "failure"));
            String uriParams = URLEncodedUtils.format(qparams, "UTF-8");
            String redirectUri = Constants.FLOCK_REDIRECT_URL + "?" + uriParams;
            try {
                response.sendRedirect(redirectUri);
            } catch (IOException ioe) {
                logger.error("Error redirecting to:{}", redirectUri);
            }
        }
    }

    private String getState(String userId) {
        Map<String, String> state = new HashMap<>();
        state.put("userid", userId);
        state.put("csrf_token", csrfTokenService.getCsrfToken(userId));
        return new Gson().toJson(state);
    }

    @RequestMapping(value = "/oauthcomplete", method = RequestMethod.GET)
    public void oauthComplete(@RequestParam String user){
        logger.info("Oauth completed for user:", user);
    }

    @RequestMapping(value = "/availability", params = {"lon","lat"}, method = RequestMethod.GET)
    public String getProducts(@RequestParam String lon, @RequestParam String lat){
        String url = "https://api.uber.com/v1/estimates/time?start_latitude=" + lat + "&start_longitude=" + lon;
        logger.info("Getting availability for long {} and lat {}", lon, lat);
        restTemplate.getForObject(url, Availability.class);
        return "Longitude:" + lon + " Latitude:" + lat;
    }

    @RequestMapping(value = "/status")
    public String getStatus(){
        return "alive";
    }

}
