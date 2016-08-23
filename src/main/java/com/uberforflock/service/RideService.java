package com.uberforflock.service;

import com.google.gson.Gson;
import com.uberforflock.model.Ride;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kumarke on 8/23/16.
 */

@Service
public class RideService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserAuthService userAuthService;

    private static final Logger logger = LoggerFactory.getLogger(RideService.class);

    public Ride bookRide(String product_id, String start_latitude, String start_longitude){
        String cabRequestUrl = "https://sandbox-api.uber.com/v1/requests";
        HttpHeaders headers = new HttpHeaders();
        Map<String, Object> postpayload = new HashMap<>();
        postpayload.put("start_latitude", start_latitude);
        postpayload.put("start_longitude", start_longitude);
        String accessToken = userAuthService.getAccessToken("kk");
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> httpEntity = new HttpEntity<>(new Gson().toJson(postpayload), headers);

        logger.info("Requesting product:{}", product_id);
        ResponseEntity<String> cabResponse = restTemplate.exchange(cabRequestUrl, HttpMethod.POST, httpEntity, String.class, Collections.emptyMap());
        RideResponse rideResponse = new Gson().fromJson(cabResponse.getBody(), RideResponse.class);
        logger.info("Cab response:{}", cabResponse.getBody());

        logger.info("Changing state to available for requestid:{}", rideResponse.getRequest_id());
        String changeStatusUrl = "https://sandbox-api.uber.com/v1/sandbox/requests/" + rideResponse.getRequest_id();
        postpayload = new HashMap<>();
        //postpayload.put("status", "accepted");
        postpayload.put("status", "arriving");
        httpEntity = new HttpEntity<>(new Gson().toJson(postpayload), headers);
        ResponseEntity<String> stateChangeResponse = restTemplate.exchange(changeStatusUrl, HttpMethod.PUT, httpEntity, String.class, Collections.emptyMap());
        logger.info("Status change response:{}", stateChangeResponse.getBody());

        logger.info("Fetching request details for requestid:{}", rideResponse.getRequest_id());
        String statusRequestUrl = "https://sandbox-api.uber.com/v1/requests/" + rideResponse.getRequest_id();
        postpayload = new HashMap<>();
        httpEntity = new HttpEntity<>(new Gson().toJson(postpayload), headers);
        ResponseEntity<String> statusResponse = restTemplate.exchange(statusRequestUrl, HttpMethod.GET, httpEntity, String.class, Collections.emptyMap());
        Ride ride = new Gson().fromJson(statusResponse.getBody(),Ride.class);
        logger.info("Status fetch response:", statusResponse.getBody());
        return ride;
    }

    private class RideResponse{
        private String status;
        private String request_id;

        public String getStatus() {
            return status;
        }

        public String getRequest_id() {
            return request_id;
        }
    }
}
