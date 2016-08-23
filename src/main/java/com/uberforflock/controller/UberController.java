package com.uberforflock.controller;


import co.flock.www.FlockEventsHandlerClient;
import co.flock.www.JWTToken;
import co.flock.www.model.JWT.JWTPayload;
import co.flock.www.model.flockevents.SlashCommand;
import com.google.gson.Gson;
import com.uberforflock.dao.OauthCredentialDao;
import com.uberforflock.model.Availability;
import com.uberforflock.model.OauthCredential;
import com.uberforflock.model.UberFlockEventHandler;
import com.uberforflock.service.*;
import com.uberforflock.util.Constants;
import com.uberforflock.util.Util;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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

    @Autowired
    private MessageService messageService;

    @Autowired
    private Oauth2TokenService tokenService;

    @Autowired
    private UserAuthService userAuthService;

    @Autowired
    private UberFlockEventHandler eventHandler;

    @Autowired
    private RideService rideService;

    @RequestMapping(value = "/authUrl", method = RequestMethod.GET)
    public String getAuthUrl(@RequestParam String userid){
        try {
            OAuthClientRequest.AuthenticationRequestBuilder requestBuilder = OAuthClientRequest
                    .authorizationLocation("https://login.uber.com/oauth/v2/authorize")
                    .setClientId("oZz5Gb_QSSe58a58C1hak9pURrnoYiU6")
                    .setRedirectURI(Constants.OAUTH_CALLBACK_URL)
                    .setScope("request profile")
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
    public String callbackHandler(@RequestParam Map<String, String> params, HttpServletResponse response){
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
                return "failed";
            }

            if (!csrfTokenService.isValidToken(csrf_token, user)) {
                logger.info("Invalid csrf Token " + csrf_token + " for the user " + user);
                return "failed";
            }
            OauthCredential credential = oauthtokenService.getOauth2Credential( code, GrantType.AUTHORIZATION_CODE);
            credential.setDisplayName("dummy");
            oauthCredentialDao.setCredential(stateMap.get("userid"), credential);
            logger.info("Saved credential for user {} for app {}", user);
            logger.info("Redirecting to:" + Constants.FLOCK_REDIRECT_URL);
            return "User Authenticated";
        } catch (Exception e) {
            logger.error("Authentication failed", e);
            return "failed";
        }
    }

    private String getState(String userId) {
        Map<String, String> state = new HashMap<>();
        state.put("userid", userId);
        state.put("csrf_token", csrfTokenService.getCsrfToken(userId));
        return new Gson().toJson(state);
    }

    @RequestMapping(value = "/availability", params = {"lon", "lat", "flockEvent"}, method = RequestMethod.GET)
    public void getAvailability(@RequestParam String lon, @RequestParam String lat, @RequestParam String flockEvent){
        try {
            CompletableFuture.supplyAsync(() -> {
                String url = "https://api.uber.com/v1/estimates/time?start_latitude=" + lat + "&start_longitude=" + lon;
                logger.info("Getting availability for long {} and lat {}", lon, lat);
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Token " + Constants.SERVER_TOKEN);
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                Availability availability = new Gson().fromJson(response.getBody(), Availability.class);
                messageService.sendAvailabilityMessage(lat, lon, availability, new Gson().fromJson(flockEvent, SlashCommand.class));
                logger.info(response.getBody());
                return null;
            });
            logger.info("Requesting cab availability.");
        }catch(Exception e){
            logger.error("Exception getting availability for flockEvent {}", flockEvent);
        }
    }

    @RequestMapping(value = "/doaouth")
    public ModelAndView doaouth(@RequestParam String flockValidationToken) throws Exception {
        try {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("oauth");
            JWTPayload jwtPayload =  JWTToken.GetJWTPayload(flockValidationToken, "bd6928c7-9439-457b-b617-ab4e3fee0b30");
            String oauthURL =  getAuthUrl(jwtPayload.getUserId());
            modelAndView.addObject("oauthURL", oauthURL);
            return  modelAndView;
        }catch(Exception e){
            logger.error("Exception getting availability for flockEvent", e);
            throw e;
        }
    }

    @RequestMapping(value = "/status")
    public String getStatus(){
        return "alive";
    }

    @RequestMapping(value = "/ride", method = RequestMethod.POST)
    public void ride(@RequestParam String product_id, @RequestParam String start_latitude, @RequestParam String start_longitude){
        try {
            rideService.bookRide(product_id, start_latitude, start_longitude);
        }catch (Exception e) {
            logger.error("Error requesting ride", e);
        }
    }

    @RequestMapping(value = "/flockeventcallback", method = RequestMethod.POST)
    public void flockEventCallbackHandler(HttpServletRequest request) throws Exception{
       try {
           FlockEventsHandlerClient eventsHandlerClient = new FlockEventsHandlerClient(eventHandler, Constants.APP_SECRET);
           eventsHandlerClient.Handle(request);
           logger.info("Received flock event");
       }catch (Exception e)
       {
           logger.error("Error handling flock event", e);
       }
    }


}
