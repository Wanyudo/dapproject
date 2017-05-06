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
    public static int classCount = 2;

    public static final String TRAINING_DATA_FILE = "trainingData.csv";
    public static final String VAILDATION_DATA_FILE = "validationData.csv";

    // file names wich contains part of the whole data
    public static final String TRAINING_DATA_FILE_FLOOR = "trainingDataFloor.csv";
    public static final String TRAINING_DATA_FILE_BUILDING_ID = "trainingDataBuildingId.csv";
    public static final String VAILDATION_DATA_FILE_FLOOR = "validationDataFloor.csv";
    public static final String VAILDATION_DATA_FILE_BUILDING_ID = "validationDataBuildingId.csv";
    public static final String HEADER_DATA_FILE_FLOOR = "headerFloor.csv";

    // csv file headers
    public static String FINGERPRINT_HEADER_FLOOR = "";
    public static String FINGERPRINT_HEADER_BUILDING_ID = "";

    // resource data
    public static List<Fingerprint> trainingData = new ArrayList<Fingerprint>();
    public static List<Fingerprint> validationData = new ArrayList<Fingerprint>();
}
