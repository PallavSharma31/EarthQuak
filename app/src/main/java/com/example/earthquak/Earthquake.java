package com.example.earthquak;

public class Earthquake {
    private Double mMagnitude;
    private String mLocation;
    private long mTimeInMilliseconds;
    private String url;

    public Earthquake(Double mMagnitude, String mLocation, long mTimeInMilliseconds,String url) {
        this.mMagnitude = mMagnitude;
        this.mLocation = mLocation;
        this.mTimeInMilliseconds = mTimeInMilliseconds;
        this.url=url;

    }

    public String getUrl() {
        return url;
    }

    public Double getmMagnitude() {
        return mMagnitude;
    }

    public String getmLocation() {
        return mLocation;
    }

    public long getmTimeInMilliseconds() {
        return mTimeInMilliseconds;
    }
}
