package com.lovbomobile.android.locsy.entities;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

import com.lovbomobile.way.entities.Location;

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
		latitude = in.readDouble();
		longitude = in.readDouble();
		clientTime = in.readLong();
		serverTime = in.readLong();
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		dest.writeLong(clientTime);
		dest.writeLong(serverTime);
	}

}
