package com.lovbomobile.android.locsy.client.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import com.google.gson.JsonSyntaxException;
import com.lovbomobile.android.locsy.client.LocsyClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lovbomobile.android.locsy.entities.ParceableLocation;
import com.lovbomobile.android.locsy.utils.LocationUtils;
import com.lovbomobile.way.entities.Location;

public class RestLocsyClient implements LocsyClient {

    private static final String MIME_TYPE_JSON = "application/json";

    private static final String HEADER_FIELD_ACCEPT = "Accept";

    private static final String HEADER_FIELD_CONTENT_TYPE = "Content-Type";

    private static final int DEFAULT_TIMEOUT = 10000;

    public HashMap<String, ParceableLocation> getFriendsLocation(String userID, String password, String serverAddress) {
        try {
            HttpGet httpGet = getHttpGetRequestForFriendsLocations(userID, password, serverAddress);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == 200) {
                HashMap<String, ParceableLocation> friendLocations = extractFriendsLocationFromHttpResponse(response);
                return friendLocations;
            }

        } catch (IOException e) {
        }

        return null;
    }

    private HttpGet getHttpGetRequestForFriendsLocations(String userID, String password, String serverAddress) {
        HttpGet httpGet = new HttpGet(getFriendListUrl(userID, password, serverAddress));
        httpGet.addHeader(HEADER_FIELD_ACCEPT, MIME_TYPE_JSON);
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, DEFAULT_TIMEOUT);
        httpGet.setParams(httpParams);
        return httpGet;
    }


    private HashMap<String, ParceableLocation> extractFriendsLocationFromHttpResponse(HttpResponse response) throws IOException {
        HashMap<String, ParceableLocation> friendLocations = null;
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
            String jsonLocations = getJsonStringFromResponse(reader);
            friendLocations = getFriendsMapFromJsonString(jsonLocations);
        }
        return friendLocations;
    }

    private HashMap<String, ParceableLocation> getFriendsMapFromJsonString(String jsonLocations) {
        HashMap<String, ParceableLocation> friendLocations = null;
        Gson gson = new Gson();
        try {
            friendLocations = gson.fromJson(jsonLocations,
                    new TypeToken<HashMap<String, ParceableLocation>>() {
                    }.getType());
        } catch (JsonSyntaxException e) {

        }
        return friendLocations;
    }

    private String getJsonStringFromResponse(BufferedReader reader) throws IOException {
        String jsonLocations = "";
        String line = null;
        while ((line = reader.readLine()) != null) {
            jsonLocations += line;
        }
        return jsonLocations;
    }



    public void sendLocationToServer(Location location, String userID, String password, String serverAddress) {
        String url = getUpdateLocationUrl(userID, password, serverAddress);
        location.clientTime = System.currentTimeMillis();

        String jsonLocation = getJsonStringFromLocation(location);

        try {
            HttpPut httpPut = getHttpPutRequestForSendingLocation(url, jsonLocation);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpPut);
            if (response.getStatusLine().getStatusCode() != 200) {
                      //TODO what do we want to do here
            }
        } catch (Exception e) {
        }
    }

    private HttpPut getHttpPutRequestForSendingLocation(String url, String jsonLocation) throws UnsupportedEncodingException {
        HttpPut httpPut = new HttpPut(url);
        httpPut.setEntity(new StringEntity(jsonLocation));
        httpPut.addHeader(HEADER_FIELD_CONTENT_TYPE, MIME_TYPE_JSON);
        httpPut.addHeader(HEADER_FIELD_ACCEPT, MIME_TYPE_JSON);
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, DEFAULT_TIMEOUT);
        httpPut.setParams(httpParams);
        return httpPut;
    }

    private String getJsonStringFromLocation(Location location) {
        Gson gson = new Gson();
        return gson.toJson(location);
    }


    private String getUpdateLocationUrl(String userID, String password, String serverAddress) {
        long timestamp = System.currentTimeMillis();
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA1");
            String hash = LocationUtils.hexBinaryToString(digest.digest((userID + password + timestamp).getBytes()));

            return serverAddress + "/locsy/rest/location/user/" + userID + "/" + timestamp + "/" + hash;
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

    }

    private String getFriendListUrl(String userID, String password, String serverAddress) {
        long timestamp = System.currentTimeMillis();
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA1");
            String hash = LocationUtils.hexBinaryToString(digest.digest((userID + password + timestamp).getBytes()));

            return serverAddress + "/locsy/rest/location/user/" + userID + "/friends/" + timestamp + "/" + hash;
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

    }

}
