package com.lovbomobile.android.locsy.test.activities;

import com.lovbomobile.android.locsy.client.LocsyClient;
import com.lovbomobile.android.locsy.entities.ParceableLocation;
import com.lovbomobile.way.entities.Location;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: bongo
 * Date: 5/26/13
 * Time: 12:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientMock implements LocsyClient {
    @Override
    public HashMap<String, ParceableLocation> getFriendsLocation(String userID, String password, String serverAddress) {
        HashMap<String,ParceableLocation> locations = new HashMap<String, ParceableLocation>();
        ParceableLocation testLocation = new ParceableLocation(0,0);
        locations.put("testPerson",testLocation);
        return locations;
    }

    @Override
    public void sendLocationToServer(Location location, String userID, String password, String serverAddress) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
