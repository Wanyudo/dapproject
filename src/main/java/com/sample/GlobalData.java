package com.sample;

import jdk.nashorn.internal.parser.JSONParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public static boolean [] wapUsefulness = new boolean[WAPS_COUNT];

    // file names wich contains part of the whole data
    public static final String TRAINING_DATA_FILE_FLOOR = "trainingDataFloor";
    public static final String TRAINING_DATA_FILE_BUILDING_ID = "trainingDataBuildingId";
    public static final String VAILDATION_DATA_FILE_FLOOR = "validationDataFloor";
    public static final String VAILDATION_DATA_FILE_BUILDING_ID = "validationDataBuildingId";

    public static final String OUTPUT_FILE_EXTENSION = ".csv";

    // csv file headers
    public static String FINGERPRINT_HEADER_FLOOR = "";
    public static String FINGERPRINT_HEADER_BUILDING_ID = "";

    // resource data
    public static List<Fingerprint> trainingData = new ArrayList<Fingerprint>();
    public static List<Fingerprint> validationData = new ArrayList<Fingerprint>();

    public static final String ALGORITHM_RESULTS_FILE = "algorithmResults.json";
    public static JSONArray algorithmResults = new JSONArray();

    public static void parseAlgorithmResultFile() {
        File f = new File(ALGORITHM_RESULTS_FILE);
        if (f.exists() && !f.isDirectory()) {
            try {
                byte[] encoded = Files.readAllBytes(Paths.get(ALGORITHM_RESULTS_FILE));
                String jsonString = new String(encoded, "UTF-8");
                if (!jsonString.isEmpty()) {
                    algorithmResults = new JSONArray(jsonString);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateAlgorithmResultsFile() throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new File(ALGORITHM_RESULTS_FILE));
        pw.write(algorithmResults.toString());
        pw.close();
    }

    public static void addAlgorithmResult(String algorithmName, double successRate, long timeMillis) throws JSONException, FileNotFoundException {
        // remove old algorithm result
        for (int i = 0, iLim = algorithmResults.length(); i < iLim; i++) {
            if (algorithmResults.getJSONObject(i).getString("name").equals(algorithmName)) {
                algorithmResults.remove(i);
            }
            break;
        }

        // add new result
        JSONObject results = new JSONObject();
        results.put("name", algorithmName);
        results.put("successRate", successRate);
        results.put("timeMillis", timeMillis);
        algorithmResults.put(results);
        updateAlgorithmResultsFile();
    }

    public static void addKNNResult(String algorithmName, int k, double successRate, double positioningMAE, long timeMillis) throws JSONException, FileNotFoundException {
        // remove old algorithm result
        for (int i = 0, iLim = algorithmResults.length(); i < iLim; i++) {
            if (algorithmResults.getJSONObject(i).has("k")) {
                if (algorithmResults.getJSONObject(i).getString("name").equals(algorithmName)
                        && (algorithmResults.getJSONObject(i).getInt("k")) == k) {
                    algorithmResults.remove(i);
                    break;
                }
            }
        }

        // add new result
        JSONObject results = new JSONObject();
        results.put("name", algorithmName);
        results.put("k", k);
        results.put("successRate", successRate);
        results.put("positioningMAE", positioningMAE);
        results.put("timeMillis", timeMillis);
        algorithmResults.put(results);
        updateAlgorithmResultsFile();
    }

    public static void addJ48Result(boolean unpruned, int minNumObj, double successRate, long timeMillis) throws JSONException, FileNotFoundException {
        // remove old algorithm result
        for (int i = 0, iLim = algorithmResults.length(); i < iLim; i++) {
            if (algorithmResults.getJSONObject(i).has("unpruned") && (algorithmResults.getJSONObject(i).has("minNumObj"))) {
                if (algorithmResults.getJSONObject(i).getString("name").equals("WekaJ48")
                        && (algorithmResults.getJSONObject(i).getBoolean("unpruned")) == unpruned
                        && (algorithmResults.getJSONObject(i).getInt("minNumObj")) == minNumObj) {
                    algorithmResults.remove(i);
                    break;
                }
            }
        }

        // add new result
        JSONObject results = new JSONObject();
        results.put("name", "WekaJ48");
        results.put("unpruned", unpruned);
        results.put("minNumObj", minNumObj);
        results.put("successRate", successRate);
        results.put("timeMillis", timeMillis);
        algorithmResults.put(results);
        updateAlgorithmResultsFile();
    }

    public static void addRandomForestResult(int maxDepth, int numIterations, double successRate, long timeMillis) throws JSONException, FileNotFoundException {
        // remove old algorithm result
        for (int i = 0, iLim = algorithmResults.length(); i < iLim; i++) {
            if (algorithmResults.getJSONObject(i).has("maxDepth") && (algorithmResults.getJSONObject(i).has("numIterations"))) {
                if (algorithmResults.getJSONObject(i).getString("name").equals("WekaJ48")
                        && (algorithmResults.getJSONObject(i).getInt("maxDepth")) == (maxDepth)
                        && (algorithmResults.getJSONObject(i).getInt("numIterations")) == numIterations) {
                    algorithmResults.remove(i);
                    break;
                }
            }
        }

        // add new result
        JSONObject results = new JSONObject();
        results.put("name", "WekaRandomForest");
        results.put("maxDepth", maxDepth);
        results.put("numIterations", numIterations);
        results.put("successRate", successRate);
        results.put("timeMillis", timeMillis);
        algorithmResults.put(results);
        updateAlgorithmResultsFile();
    }
}


