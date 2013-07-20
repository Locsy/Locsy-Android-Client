package com.lovbomobile.android.locsy.test.activities;

import android.location.Location;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.view.ViewGroup;
import com.lovbomobile.android.locsy.R;
import com.lovbomobile.android.locsy.activities.LocsyApplication;
import com.lovbomobile.android.locsy.activities.MainActivity;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.lovbomobile.android.locsy.test.activities.MainActivityTest \
 * com.lovbomobile.android.locsy.tests/android.test.InstrumentationTestRunner
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    MainActivity mainActivity;

    public MainActivityTest() {
        super("com.lovbomobile.android.locsy.activities", MainActivity.class);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LocsyApplication.setLocsyClient(new ClientMock());
    }



    @UiThreadTest
    public void testGetLocations() {
       mainActivity = getActivity();
       ViewGroup userArea = (ViewGroup)mainActivity.findViewById(R.id.userArea);
       mainActivity.onLocationChanged(getMockLocation(0,0));
        assertEquals("userArea should contain at least my and my friends position",2, userArea.getChildCount());

    }

    private Location getMockLocation(double latitude, double longitude) {
        Location location = new Location("mock");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(0);
        return  location;
    }

}
