package com.sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

import static com.sample.GlobalData.*;
import static com.sample.PublicMethods.addItemToRepetitions;

/**
 * Created by Julee on 10.04.2017.
 */
public class KnnAlgorithm {
    public static void sortNeighbors(final Fingerprint validationFingerprint) {
        ArrayList<NeighborData> neighbors = new ArrayList<NeighborData>();
        double distanceMin = 1000000;
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
                } else if (neighbor1.getDistance() > neighbor2.getDistance()){
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        addNeighbors(validationData.indexOf(validationFingerprint), neighbors);
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

    private static synchronized void addNeighbors(int validationFingerprintId, ArrayList<NeighborData> neighbors) {
        neighborList.set(validationFingerprintId, neighbors);
    }

    // predicts location data to all validation examples calculates absolute error and prints it to test.csv
    public static void doPrediction(int k, boolean withWeighting) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new File("test.csv"));
        StringBuilder sb = new StringBuilder();
        double longitudeAbsError = 0, latitudeAbsError = 0;

        int correctPredictionsCount = 0;

        if (withWeighting) {
            for (int i = 0; i < validationDataCount; i++) {
                LocationData data = predictLocationDataWithWeighting(i, k);
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
                LocationData data = predictLocationData(i, k);
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
        sb.append("longitude absolute error: " + longitudeAbsError);
        sb.append('\n');
        sb.append("latitude absolute error: " + latitudeAbsError);
        sb.append('\n');
        sb.append("sucess rate: " + Double.toString((double )correctPredictionsCount / validationDataCount * 100) + "%");
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
            addItemToRepetitions(neighbor.locationData.getFloor(), floorValues);
            addItemToRepetitions(neighbor.locationData.getBuildingId(), buildingIdValues);
        }
        longitude /= k;
        latitude /= k;
        return new LocationData(longitude, latitude, getMostlyRepeatedItem(floorValues), getMostlyRepeatedItem(buildingIdValues));
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
            double weight = 1 / neighborList.get(validationFingerprintId).get(i).getDistance();
            weightSum += weight;
            // Regression result calculation
            longitude += neighbor.locationData.getLongitude() * weight;
            latitude += neighbor.locationData.getLatitude() * weight;
            // Classification result calculation
            addItemAndRepetitionCountToRepetitionsWithWeighing(neighbor.locationData.getFloor(), floorValues, weight);
            addItemAndRepetitionCountToRepetitionsWithWeighing(neighbor.locationData.getBuildingId(), buildingIdValues, weight);
        }
        longitude /= weightSum;
        latitude /= weightSum;
        return new LocationData(longitude, latitude, getMostlyRepeatedItemWithWeighing(floorValues), getMostlyRepeatedItemWithWeighing(buildingIdValues));
    }

    private static void addItemAndRepetitionCountToRepetitionsWithWeighing(int item, HashMap<Integer, Double> repetitions, double weight) {
        if (repetitions.containsKey(item)) {
            repetitions.put(item, repetitions.get(item) + weight);
        } else {
            repetitions.put(item, weight);
        }
    }

    private static int getMostlyRepeatedItem(HashMap<Integer, Integer> repetitions) {
        int maxRepetitionsCount = 0;
        int mostlyRepeatedItem = 0;
        for (Map.Entry<Integer, Integer> e : repetitions.entrySet()) {
            if (e.getValue() > maxRepetitionsCount) {
                maxRepetitionsCount = e.getValue();
                mostlyRepeatedItem = e.getKey();
            }
        }
        return mostlyRepeatedItem;
    }

    private static int getMostlyRepeatedItemWithWeighing(HashMap<Integer, Double> repetitions) {
        double maxRepetitionsCount = 0;
        int mostlyRepeatedItem = 0;
        for (Map.Entry<Integer, Double> e : repetitions.entrySet()) {
            if (e.getValue() > maxRepetitionsCount) {
                maxRepetitionsCount = e.getValue();
                mostlyRepeatedItem = e.getKey();
            }
        }
        return mostlyRepeatedItem;
    }
}
