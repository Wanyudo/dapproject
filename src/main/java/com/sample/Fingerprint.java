package com.sample;

/**
 * Created by Julee on 07.04.2017.
 */
public class Fingerprint {
    // Signal intensities received from the WAPs in dBm
    public int [] wapSignalIntensities;

    // Data to be predicted
    public LocationData locationData;

    public Fingerprint(int [] wapSignalIntensities, double longitude, double latitude, int floor, int buildingId) {
        this.wapSignalIntensities = wapSignalIntensities;
        this.locationData = new LocationData(longitude, latitude, floor, buildingId);
    }
}
