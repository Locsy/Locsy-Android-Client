package com.lovbomobile.android.locsy.activities;

import com.lovbomobile.android.locsy.client.LocsyClient;
import com.lovbomobile.android.locsy.client.impl.RestLocsyClient;

/**
 * Created with IntelliJ IDEA.
 * User: bongo
 * Date: 6/23/13
 * Time: 6:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class LocsyApplication {

    private static LocsyClient locsyClient;

    public static void onCreate() {
        if (locsyClient == null) {
            locsyClient = new RestLocsyClient();
        }
    }

    public static LocsyClient getLocsyClient() {
        return locsyClient;
    }

    public static void setLocsyClient(LocsyClient locsyClient) {
        LocsyApplication.locsyClient = locsyClient;
    }
}
