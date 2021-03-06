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

package com.lovbomobile.android.locsy.entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class ParceableLocation extends Location implements Parcelable, Serializable {

	private static final long serialVersionUID = 12348782344L;

	public static final Parcelable.Creator<ParceableLocation> CREATOR = new Parcelable.Creator<ParceableLocation>() {
		public ParceableLocation createFromParcel(Parcel in) {
			return new ParceableLocation(in);
		}

		public ParceableLocation[] newArray(int size) {
			return new ParceableLocation[size];
		}
	};

	public int describeContents() {

		return 0;
	}

	public ParceableLocation(double latitude, double longitude) {
		super(latitude, longitude);
	};

	public ParceableLocation(Parcel in) {
		setLatitude(in.readDouble());
		setLongitude(in.readDouble());
		setClientTime(in.readLong());
		setServerTime(in.readLong());
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(getLatitude());
		dest.writeDouble(getLongitude());
		dest.writeLong(getClientTime());
		dest.writeLong(getServerTime());
	}

}
