/**
 Copyright (c) 2013 Sven Schindler

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */

package com.lovbomobile.android.locsy.test.activities;

import android.location.Location;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.view.ViewGroup;
import com.lovbomobile.android.locsy.R;
import com.lovbomobile.android.locsy.activities.LocsyApplication;
import com.lovbomobile.android.locsy.activities.MainActivity;

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
