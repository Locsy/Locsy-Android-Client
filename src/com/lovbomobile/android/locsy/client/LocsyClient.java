package com.lovbomobile.android.locsy.client;

import java.util.HashMap;

import com.lovbomobile.android.locsy.entities.ParceableLocation;
import com.lovbomobile.way.entities.Location;

public interface LocsyClient {

	HashMap<String, ParceableLocation> getFriendsLocation(String userID, String password, String serverAddress);

	void sendLocationToServer(Location location, String userID, String password, String serverAddress);
}
