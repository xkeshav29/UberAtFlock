package com.uberforflock.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kumarke on 8/23/16.
 */
public class Util {

    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    public static Map<String, String> parseJsonStringToMap(String json) {
        if (json == null)
            return new HashMap<>();
        Gson gson = new GsonBuilder().create();
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public static void redirect(HttpServletResponse response, String baseUrl, List<NameValuePair> qparams) {
        String uriParams = URLEncodedUtils.format(qparams, "UTF-8");
        String redirectUri = baseUrl + "?" + uriParams;
        try {
            logger.info("Redirecting to: {}", redirectUri);
            response.sendRedirect(redirectUri);
        } catch (IOException ioe) {
            logger.error("Error redirecting to:{}" + redirectUri, ioe);
            throw new RuntimeException("Some error occurred. Please Retry");
        }
    }
}
