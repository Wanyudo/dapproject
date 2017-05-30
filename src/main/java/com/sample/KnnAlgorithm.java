package com.sample;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static com.sample.GlobalData.OUTPUT_FILE_EXTENSION;
import static com.sample.GlobalData.WAPS_COUNT;
import static com.sample.GlobalData.addKNNResult;
import static com.sample.GlobalData.trainingData;
import static com.sample.GlobalData.validationData;
import static com.sample.GlobalData.validationDataCount;
import static com.sample.PublicMethods.*;
import static com.sample.PublicMethods.addIntegerToRepetitionsWithWeighing;
import static com.sample.PublicMethods.getMostlyRepeatedIntegerWeighing;

/**
 * Created by Julee on 10.04.2017.
 */
public class KnnAlgorithm {
    public static final String OUTPUT_FILE = "outputKNN";

    public static List<ArrayList<NeighborData>> neighborList = new ArrayList<ArrayList<NeighborData>>();

    // to predict time algorithm takes for training and prediction
    private static long trainingTime;

    // sequential (very slow)
    public static void prepareKnnData() {
        for (int i = 0, iLim = validationDataCount; i < iLim; i++) {
            neighborList.add(null);
        }

        for (Fingerprint validationFingerprint : validationData) {
            sortNeighbors(validationFingerprint);
        }
    }

    // parallel
    public static void prepareKnnDataParallel() {
        long start = System.currentTimeMillis();

        for (int i = 0, iLim = validationDataCount; i < iLim; i++) {
            neighborList.add(null);
        }

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(validationData.size());
        Collection<Future<?>> futures = new LinkedList<Future<?>>();
        for (Fingerprint validationFingerprint : validationData) {
            futures.add(executor.submit(addSortNeighborsTask(validationFingerprint)));
        }
        // wait until all of the threads are finished
        for (Future<?> future:futures) {
            try {
                future.get();
            } catch (InterruptedException e) {

            } catch (ExecutionException e) {

            }
        }

        long end = System.currentTimeMillis();
        trainingTime = end - start;
    }

    public static WpsTask addSortNeighborsTask(final Fingerprint validationFingerprint) {
        final WpsTask task = new WpsTask() {
            @Override
            protected void doTask() {
                sortNeighbors(validationFingerprint);
            }
        };
        return task;
    }

    public static void sortNeighbors(final Fingerprint validationFingerprint) {
        ArrayList<NeighborData> neighbors = new ArrayList<NeighborData>();
        for (Fingerprint trainingFingerprint : trainingData)  {
            double distance = calculateEuclideanDistance(validationFingerprint, trainingFingerprint);
            neighbors.add(new NeighborData(trainingData.indexOf(trainingFingerprint), distance));
        }

        // sort neighbors by distance
        Collections.sort(neighbors, new Comparator<NeighborData>() {
            @Override
            public int compare(NeighborData neighbor1, NeighborData neighbor2)
            {
                if (neighbor1.getDistance() == neighbor2.getDistance()) {
                    return 0;
                } else if (neighbor1.getDistance() > neighbor2.getDistance()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        addOrederedNeighbors(validationData.indexOf(validationFingerprint), neighbors);
    }

    // calculates the Euclidean distance
    private static double calculateEuclideanDistance(Fingerprint validationFingerprint, Fingerprint trainingFingerprint) {
        double distance = 0;
        for (int i = 0; i < WAPS_COUNT; i++) {
            distance += Math.pow(validationFingerprint.wapSignalIntensities[i] - trainingFingerprint.wapSignalIntensities[i], 2);
        }
        distance = Math.sqrt(distance);
        return distance;
    }

    private synchronized static void addOrederedNeighbors(int validationFingerprintId, ArrayList<NeighborData> neighbors) {
        neighborList.set(validationFingerprintId, neighbors);
    }

    public static final int KNN_DEFAULT = 0;
    public static final int KNN_WEIGHTING = 1;
    public static final int KNN_VOTING = 2;

    // predicts location data to all validation examples calculates absolute error and prints it to test.csv
    public static void doPrediction(int k, int method) throws FileNotFoundException, JSONException {
        long start = System.currentTimeMillis();

        PrintWriter pw = null;
        StringBuilder sb = new StringBuilder();
        double positioningMAE = 0;
        double correctPredictionsCount = 0;

        sb.append("BuildingId,Floor,Latitude,Longitude");
        sb.append('\n');

        String algorithmName = "";
        switch (method) {
            case KNN_WEIGHTING: // weighting
                for (int i = 0; i < validationDataCount; i++) {
                    pw = new PrintWriter(new File(OUTPUT_FILE + "defaultWeighting_k_" + k + OUTPUT_FILE_EXTENSION));
                    LocationData data = predictLocationDataWeighting(i, k);
                    sb.append(data.getBuildingId());
                    sb.append(',');
                    sb.append(data.getFloor());
                    sb.append(',');
                    sb.append(data.getLatitude());
                    sb.append(',');
                    sb.append(data.getLongitude());
                    sb.append('\n');

                    if (validationData.get(i).locationData.getBuildingId() == data.getBuildingId() && validationData.get(i).locationData.getFloor() == data.getFloor()) {
                        positioningMAE += Math.sqrt(Math.pow(validationData.get(i).locationData.getLongitude() - data.getLongitude(), 2) +
                                Math.pow(validationData.get(i).locationData.getLatitude() - data.getLatitude(), 2));
                        correctPredictionsCount++;
                    }
                }
                algorithmName = "kNN_weighting";
                break;
            case KNN_VOTING: // voting
                for (int i = 0, iLim = validationDataCount; i < iLim; i++) {
                    pw = new PrintWriter(new File(OUTPUT_FILE + "voting_k_" + k + OUTPUT_FILE_EXTENSION));
                    LocationData data = predictLocationDataVoting(i, k);
                    sb.append(data.getBuildingId());
                    sb.append('\n');
                    sb.append(data.getFloor());
                    sb.append(',');
                    sb.append(data.getLatitude());
                    sb.append(',');
                    sb.append(data.getLongitude());
                    sb.append(',');

                    if (validationData.get(i).locationData.getBuildingId() == data.getBuildingId() && validationData.get(i).locationData.getFloor() == data.getFloor()) {
                        positioningMAE += Math.sqrt(Math.pow(validationData.get(i).locationData.getLongitude() - data.getLongitude(), 2) +
                                Math.pow(validationData.get(i).locationData.getLatitude() - data.getLatitude(), 2));

                        correctPredictionsCount++;
                    }
                }
                algorithmName = "kNN_voting";
                break;
            default:
                for (int i = 0, iLim = validationDataCount; i < iLim; i++) {
                    pw = new PrintWriter(new File(OUTPUT_FILE + "default_k_" + k + OUTPUT_FILE_EXTENSION));
                    LocationData data = predictLocationData(i, k);
                    sb.append(data.getBuildingId());
                    sb.append('\n');
                    sb.append(data.getFloor());
                    sb.append(',');
                    sb.append(data.getLatitude());
                    sb.append(',');
                    sb.append(data.getLongitude());
                    sb.append(',');

                    if (validationData.get(i).locationData.getBuildingId() == data.getBuildingId() && validationData.get(i).locationData.getFloor() == data.getFloor()) {
                        positioningMAE += Math.sqrt(Math.pow(validationData.get(i).locationData.getLongitude() - data.getLongitude(), 2) +
                                Math.pow(validationData.get(i).locationData.getLatitude() - data.getLatitude(), 2));
                        correctPredictionsCount++;
                    }
                }
                algorithmName = "kNN";
                break;                
        }

        pw.write(sb.toString());
        pw.close();

        positioningMAE /= correctPredictionsCount;
        double successRate = correctPredictionsCount / validationDataCount * 100;

        long end = System.currentTimeMillis();

        addKNNResult(algorithmName, k, successRate, positioningMAE, trainingTime + end - start);
    }

    private static LocationData predictLocationData(int validationFingerprintId, int k) {
        double longitude = 0;
        double latitude = 0;
        HashMap<Integer, Integer> floorValues = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> buildingIdValues = new HashMap<Integer, Integer>();
        for (int i = 0; i < k; i++) {
            Fingerprint neighbor = trainingData.get(neighborList.get(validationFingerprintId).get(i).getId());
            // Regression result calculation
            longitude += neighbor.locationData.getLongitude();
            latitude += neighbor.locationData.getLatitude();
            // Classification result calculation
            addIntegerToRepetitions(neighbor.locationData.getFloor(), floorValues);
            addIntegerToRepetitions(neighbor.locationData.getBuildingId(), buildingIdValues);
        }
        longitude /= k;
        latitude /= k;
        return new LocationData(longitude, latitude, getMostlyRepeatedInteger(floorValues), getMostlyRepeatedInteger(buildingIdValues));
    }

    // predicts location data using weighting (inverse Euclidean distance)
    private static LocationData predictLocationDataWeighting(int validationFingerprintId, int k) {
        double longitude = 0;
        double latitude = 0;
        HashMap<Integer, Double> floorValues = new HashMap<Integer, Double>();
        HashMap<Integer, Double> buildingIdValues = new HashMap<Integer, Double>();
        double weightSum = 0;
        for (int i = 0; i < k; i++) {
            Fingerprint neighbor = trainingData.get(neighborList.get(validationFingerprintId).get(i).getId());
            double weight = 1.0 / neighborList.get(validationFingerprintId).get(i).getDistance() ;
            weightSum += weight;
            // Regression result calculation
            longitude += neighbor.locationData.getLongitude() * weight;
            latitude += neighbor.locationData.getLatitude() * weight;
            // Classification result calculation
            addIntegerToRepetitionsWithWeighing(neighbor.locationData.getFloor(), floorValues, weight);
            addIntegerToRepetitionsWithWeighing(neighbor.locationData.getBuildingId(), buildingIdValues, weight);
        }
        longitude /= weightSum;
        latitude /= weightSum;
        return new LocationData(longitude, latitude, getMostlyRepeatedIntegerWeighing(floorValues), getMostlyRepeatedIntegerWeighing(buildingIdValues));
    }

    // when there are more then k nearest neighbors (with the same distance), take them all into account
    private static LocationData predictLocationDataVoting(int validationFingerprintId, int k) {
        HashMap<Integer, Integer> floorValues = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> buildingIdValues = new HashMap<Integer, Integer>();

        // Classification result calculation
        for (int i = 0, iLim = k - 1; i < iLim; i++) {
            Fingerprint neighbor = trainingData.get(neighborList.get(validationFingerprintId).get(i).getId());
            addIntegerToRepetitions(neighbor.locationData.getFloor(), floorValues);
            addIntegerToRepetitions(neighbor.locationData.getBuildingId(), buildingIdValues);
        }

        // If neighbor next to the k-neighbor are on the same distance, use voting to choose k-neighbor
        double neighborDistanceNext = 0;
        boolean checkNext = true;
        int kReal = k;
        HashMap<Integer, Integer> floorValuesKNeighbor = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> buildingIdValuesKNeighbor = new HashMap<Integer, Integer>();
        double kNeighborDistance = neighborList.get(validationFingerprintId).get(k - 1).getDistance();
        Fingerprint neighborK = trainingData.get(neighborList.get(validationFingerprintId).get(k - 1).getId());
        addIntegerToRepetitions(neighborK.locationData.getFloor(), floorValuesKNeighbor);
        addIntegerToRepetitions(neighborK.locationData.getBuildingId(), buildingIdValuesKNeighbor);
        while (checkNext) {
            neighborDistanceNext = neighborList.get(validationFingerprintId).get(kReal).getDistance();
            if (neighborDistanceNext == kNeighborDistance) {
                Fingerprint neighbor = trainingData.get(neighborList.get(validationFingerprintId).get(kReal).getId());
                addIntegerToRepetitions(neighbor.locationData.getFloor(), floorValuesKNeighbor);
                addIntegerToRepetitions(neighbor.locationData.getBuildingId(), buildingIdValuesKNeighbor);
                kReal++;
            } else {
                checkNext = false;
            }
        }
        addIntegerToRepetitions(getMostlyRepeatedInteger(floorValuesKNeighbor), floorValues);
        addIntegerToRepetitions(getMostlyRepeatedInteger(buildingIdValuesKNeighbor), buildingIdValues);

        // Regression result calculation
        int floor = getMostlyRepeatedInteger(floorValues);
        int buildingId = getMostlyRepeatedInteger(buildingIdValues);
        double longitude = 0;
        double latitude = 0;
        int majorNeighborCount = 0;
        for (int i = 0; i < kReal; i++) {
            Fingerprint neighbor = trainingData.get(neighborList.get(validationFingerprintId).get(i).getId());
            if (neighbor.locationData.getFloor() == floor && neighbor.locationData.getBuildingId() == buildingId) {
                longitude += neighbor.locationData.getLongitude();
                latitude += neighbor.locationData.getLatitude();
                majorNeighborCount++;
            }
        }
        longitude /= majorNeighborCount;
        latitude /= majorNeighborCount;

        return new LocationData(longitude, latitude, floor, buildingId);
    }
}
