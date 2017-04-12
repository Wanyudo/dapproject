package com.sample;

/**
 * Created by Julee on 10.04.2017.
 */
public class NeighborData {
    private int id;
    private double distance;

    NeighborData(int id, double distance) {
        this.id = id;
        this.distance = distance;
    }

    public int getId() {
        return id;
    }

    public double getDistance() {
        return distance;
    }
}
