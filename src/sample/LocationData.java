package sample;

/**
 * Created by Julee on 07.04.2017.
 */
public class LocationData {
    // Data to be predicted
    private double longitude;
    private double latitude;
    private int floor;
    private int buildingId;

    public LocationData(double longitude, double latitude, int floor, int buildingId) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.floor = floor;
        this.buildingId = buildingId;
    }
}
