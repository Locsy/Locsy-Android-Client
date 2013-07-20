package com.lovbomobile.android.locsy.services;

import android.location.Location;
import com.lovbomobile.android.locsy.entities.ParceableLocation;

/**
 * Created with IntelliJ IDEA.
 * User: bongo
 * Date: 5/1/13
 * Time: 9:35 AM
 * To change this template use File | Settings | File Templates.
 */
public interface MapManager {

    void updateSingleFriendsLocationOnMap(String friendsName, ParceableLocation friendsLocation);

    void updateMyCurrentPositionOnMap(Location location);

    void removeLocationOfFriend(String friendsName);

    void animateMapToFriendsLocation(String friendsName);

    void animateMapToMyLocation();

    void animateMapToMyLocationAndZoomIn();
}
