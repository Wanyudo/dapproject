package com.sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by Julee on 07.04.2017.
 */
public class GlobalData {
    public static final int WAPS_COUNT = 520;
    public static int trainingDataCount = 0;
    public static int validationDataCount = 0;

    public static final String TRAINING_DATA_FILE = "trainingData.csv";
    public static final String VAILDATION_DATA_FILE = "validationData.csv";

    // resource data
    public static List<Fingerprint> trainingData = new ArrayList<Fingerprint>();
    public static List<Fingerprint> validationData = new ArrayList<Fingerprint>();

    // prediction result data
    public static List<LocationData> knnData = new ArrayList<LocationData>();
    public static List<LocationData> naiveBayesData = new ArrayList<LocationData>();
    public static List<LocationData> naiveBayesWekaData = new ArrayList<LocationData>();
    public static List<LocationData> j48WekaData = new ArrayList<LocationData>();
    public static List<LocationData> randomForestWekaData = new ArrayList<LocationData>();

    // helper data
    public static List<ArrayList<NeighborData>> neighborList = new ArrayList<ArrayList<NeighborData>>();
}
