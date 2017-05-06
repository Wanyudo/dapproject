package com.sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static com.sample.GlobalData.*;
import static com.sample.PublicMethods.*;

/**
 * Created by Julee on 10.04.2017.
 */
public class KnnAlgorithm {
    public static final String OUTPUT_FILE = "outputKNN.csv";

    public static List<ArrayList<NeighborData>> neighborList = new ArrayList<ArrayList<NeighborData>>();



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

    // predicts location data to all validation examples calculates absolute error and prints it to test.csv
    public static void doPrediction(int k, boolean withWeighting) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new File(OUTPUT_FILE + "_k_" + k));
        StringBuilder sb = new StringBuilder();
        double longitudeAbsError = 0, latitudeAbsError = 0;

        int correctPredictionsCount = 0;

        if (withWeighting) {
            for (int i = 0; i < validationDataCount; i++) {
                LocationData data = predictLocationDataWotingWithWeighting(i, k);
                sb.append(data.getLongitude());
                sb.append(',');
                sb.append(data.getLatitude());
                sb.append(',');
                sb.append(data.getFloor());
                sb.append(',');
                sb.append(data.getBuildingId());
                sb.append('\n');

                if (validationData.get(i).locationData.getBuildingId() == data.getBuildingId() && validationData.get(i).locationData.getFloor() == data.getFloor()) {
                    longitudeAbsError += Math.abs(validationData.get(i).locationData.getLongitude() - data.getLongitude());
                    latitudeAbsError += Math.abs(validationData.get(i).locationData.getLatitude() - data.getLatitude());
                    correctPredictionsCount++;
                }
            }
        } else {
            for (int i = 0, iLim = validationDataCount; i < iLim; i++) {
                LocationData data = predictLocationDataWoting(i, k);
                sb.append(data.getLongitude());
                sb.append(',');
                sb.append(data.getLatitude());
                sb.append(',');
                sb.append(data.getFloor());
                sb.append(',');
                sb.append(data.getBuildingId());
                sb.append('\n');

                if (validationData.get(i).locationData.getBuildingId() == data.getBuildingId() && validationData.get(i).locationData.getFloor() == data.getFloor()) {
                    longitudeAbsError += Math.abs(validationData.get(i).locationData.getLongitude() - data.getLongitude());
                    latitudeAbsError += Math.abs(validationData.get(i).locationData.getLatitude() - data.getLatitude());
                    correctPredictionsCount++;
                }
            }
        }

        longitudeAbsError /= correctPredictionsCount;
        latitudeAbsError /= correctPredictionsCount;
        double successRate = (double) correctPredictionsCount / validationDataCount * 100;
        sb.append("longitude absolute error: " + longitudeAbsError);
        sb.append('\n');
        sb.append("latitude absolute error: " + latitudeAbsError);
        sb.append('\n');
        sb.append("sucess rate: " + successRate + "%");
        sb.append('\n');
        pw.write(sb.toString());
        pw.close();
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
    private static LocationData predictLocationDataWithWeighting(int validationFingerprintId, int k) {
        double longitude = 0;
        double latitude = 0;
        HashMap<Integer, Double> floorValues = new HashMap<Integer, Double>();
        HashMap<Integer, Double> buildingIdValues = new HashMap<Integer, Double>();
        double weightSum = 0;
        for (int i = 0; i < k; i++) {
            Fingerprint neighbor = trainingData.get(neighborList.get(validationFingerprintId).get(i).getId());
            double weight = 1.0 / neighborList.get(validationFingerprintId).get(i).getDistance();
            weightSum += weight;
            // Regression result calculation
            longitude += neighbor.locationData.getLongitude() * weight;
            latitude += neighbor.locationData.getLatitude() * weight;
            // Classification result calculation
            addIntegerAndRepetitionCountToRepetitionsWithWeighing(neighbor.locationData.getFloor(), floorValues, weight);
            addIntegerAndRepetitionCountToRepetitionsWithWeighing(neighbor.locationData.getBuildingId(), buildingIdValues, weight);
        }
        longitude /= weightSum;
        latitude /= weightSum;
        return new LocationData(longitude, latitude, getMostlyRepeatedIntegerWithWeighing(floorValues), getMostlyRepeatedIntegerWithWeighing(buildingIdValues));
    }

    // when there are more then k nearest neighbors (with the same distance), take them all into account
    private static LocationData predictLocationDataWoting(int validationFingerprintId, int k) {
        HashMap<Integer, Integer> floorValues = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> buildingIdValues = new HashMap<Integer, Integer>();
        HashMap<String, Integer> floorBuildingIdValues = new HashMap<>();

        double neighborDistanceNext;
        // Classification result calculation
        for (int i = 0; i < k; i++) {
            double neighborDistance = neighborList.get(validationFingerprintId).get(i).getDistance();
            Fingerprint neighbor = trainingData.get(neighborList.get(validationFingerprintId).get(i).getId());
            addIntegerToRepetitions(neighbor.locationData.getFloor(), floorValues);
            addIntegerToRepetitions(neighbor.locationData.getBuildingId(), buildingIdValues);

            addStringToRepetitions("" + neighbor.locationData.getFloor() + ";" + neighbor.locationData.getBuildingId(), floorBuildingIdValues);


            // if the next neighbor is on the same distance, take into account
            if (i < trainingDataCount - 1) {
                neighborDistanceNext = neighborList.get(validationFingerprintId).get(i + 1).getDistance();
                if (neighborDistanceNext == neighborDistance) {
                    k++;
                }
            }
        }

        String[] floorBuildingIdString = getMostlyRepeatedString(floorBuildingIdValues).split(";"); // use semicolon as separator;
        int floor = Integer.parseInt(floorBuildingIdString[0]);
        int buildingId = Integer.parseInt(floorBuildingIdString[1]);

        // Regression result calculation
        double longitude = 0;
        double latitude = 0;
        int winningNeighborCount = 0;
        for (int i = 0; i < k; i++) {
            Fingerprint neighbor = trainingData.get(neighborList.get(validationFingerprintId).get(i).getId());
            if (neighbor.locationData.getFloor() == floor && neighbor.locationData.getBuildingId() == buildingId) {
                longitude += neighbor.locationData.getLongitude();
                latitude += neighbor.locationData.getLatitude();
                winningNeighborCount++;
            }
        }
        longitude /= winningNeighborCount;
        latitude /= winningNeighborCount;

        return new LocationData(longitude, latitude, floor, buildingId);
    }

    // predicts location data using weighting (inverse Euclidean distance)
    private static LocationData predictLocationDataWotingWithWeighting(int validationFingerprintId, int k) {
        HashMap<Integer, Double> floorValues = new HashMap<Integer, Double>();
        HashMap<Integer, Double> buildingIdValues = new HashMap<Integer, Double>();
        HashMap<String, Double> floorBuildingIdValues = new HashMap<>();

        double weightSum = 0;
        double neighborDistanceNext;
        // Classification result calculation
        for (int i = 0; i < k; i++) {
            double neighborDistance = neighborList.get(validationFingerprintId).get(i).getDistance();
            Fingerprint neighbor = trainingData.get(neighborList.get(validationFingerprintId).get(i).getId());
            double weight = 1.0 / neighborList.get(validationFingerprintId).get(i).getDistance();
            addIntegerAndRepetitionCountToRepetitionsWithWeighing(neighbor.locationData.getFloor(), floorValues, weight);
            addIntegerAndRepetitionCountToRepetitionsWithWeighing(neighbor.locationData.getBuildingId(), buildingIdValues, weight);
            addStringAndRepetitionCountToRepetitionsWithWeighing("" + neighbor.locationData.getFloor() + ";" + neighbor.locationData.getBuildingId(), floorBuildingIdValues, weight);

            // if the next neighbor is on the same distance, take into account
            if (i < trainingDataCount - 1) {
                neighborDistanceNext = neighborList.get(validationFingerprintId).get(i + 1).getDistance();
                if (neighborDistanceNext == neighborDistance) {
                    k++;
                }
            }
        }

        String[] floorBuildingIdString = getMostlyRepeatedStringWeighting(floorBuildingIdValues).split(";"); // use semicolon as separator;
        int floor = Integer.parseInt(floorBuildingIdString[0]);
        int buildingId = Integer.parseInt(floorBuildingIdString[1]);

        double longitude = 0;
        double latitude = 0;
        // Regression result calculation
        for (int i = 0; i < k; i++) {
            Fingerprint neighbor = trainingData.get(neighborList.get(validationFingerprintId).get(i).getId());
            if (neighbor.locationData.getFloor() == floor && neighbor.locationData.getBuildingId() == buildingId) {
                double weight = 1.0 / neighborList.get(validationFingerprintId).get(i).getDistance();
                longitude += neighbor.locationData.getLongitude() * weight;
                latitude += neighbor.locationData.getLatitude() * weight;
                weightSum += weight;
            }
        }
        longitude /= weightSum;
        latitude /= weightSum;

        return new LocationData(longitude, latitude, floor, buildingId);
    }
}
