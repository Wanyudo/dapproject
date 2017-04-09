package sample;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Julee on 07.04.2017.
 */
public class GlobalData {
    public static final int WAPS_COUNT = 520;

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
    public static List<ArrayList<Integer>> neighborList = new ArrayList<ArrayList<Integer>>();
}