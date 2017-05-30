package com.sample;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.sample.GlobalData.*;

/**
 * Created by Julee on 11.04.2017.
 */

public class NaiveBayesAlgorithm {
    private static ArrayList<ClassProbabilityData> inputsProbabilityDataFloor; // stores mean & variance for wap grouped by classes
    private static ArrayList<ClassProbabilityData> inputsProbabilityDataBuildingId; // stores mean & variance for wap grouped by classes
    public static final String OUTPUT_FILE = "outputNaiveBayes.csv";


    public static void prepareNaiveBayesData() {
        ArrayList<ClassData> classifiedByFloorFingerprints = new ArrayList<ClassData>(); // keeps class output value and number of fingerprints from training data
        ArrayList<ClassData> classifiedByBuildingIdFingerprints = new ArrayList<ClassData>(); // keeps class output value and number of fingerprints from training data

        // group fingerprints from trainingData by floor value
        for (Fingerprint fingerprint : trainingData) {
            int floor = fingerprint.locationData.getFloor();
            int fingerprintId = trainingData.indexOf(fingerprint);
            int classDataId = getClassNumber(classifiedByFloorFingerprints, floor);
            if (classDataId == -1) {
                classifiedByFloorFingerprints.add(new ClassData(floor, fingerprintId));
            } else {
                classifiedByFloorFingerprints.get(classDataId).addFingerprintId(fingerprintId);
            }
        }

        // group fingerprints from trainingData by building id
        for (Fingerprint fingerprint : trainingData) {
            int buildingId = fingerprint.locationData.getBuildingId();
            int fingerprintId = trainingData.indexOf(fingerprint);
            int classDataId = getClassNumber(classifiedByBuildingIdFingerprints, buildingId);
            if (classDataId == -1) {
                classifiedByBuildingIdFingerprints.add(new ClassData(buildingId, fingerprintId));
            } else {
                classifiedByBuildingIdFingerprints.get(classDataId).addFingerprintId(fingerprintId);
            }
        }

        // calculate mean and variance
        inputsProbabilityDataFloor = new ArrayList<ClassProbabilityData>();
        for (ClassData classData : classifiedByFloorFingerprints) {
            int classId = classifiedByFloorFingerprints.indexOf(classData);
            int fingerprintCount = classData.getCount();
            inputsProbabilityDataFloor.add(new ClassProbabilityData(classData.getOutputValue(), (double)fingerprintCount / trainingDataCount));

            for (int i = 0; i < WAPS_COUNT; i++) {
                double mean = 0;
                HashMap<Integer, Integer> inputValueRepetitions = new HashMap<>();
                for (int fingerprintId : classData.indices) {
                    int wapSignalIntensity = trainingData.get(fingerprintId).wapSignalIntensities[i];
                    mean += wapSignalIntensity;
                }
                mean /= fingerprintCount;

                // calculate variance
                double variance = 0;
                for (int fingerprintId : classData.indices) {
                    int wapSignalIntensity = trainingData.get(fingerprintId).wapSignalIntensities[i];
                    variance += Math.pow(wapSignalIntensity - mean, 2);
                }
                variance = variance / (fingerprintCount - 1) + 0.00001; // Avoid propigation -infinty when the probability is zero

                inputsProbabilityDataFloor.get(classId).addInputProbabilityData(new ClassProbabilityData.InputProbabilityData(mean, variance));
            }
        }

        inputsProbabilityDataBuildingId = new ArrayList<ClassProbabilityData>();
        for (ClassData classData : classifiedByBuildingIdFingerprints) {
            int classId = classifiedByBuildingIdFingerprints.indexOf(classData);
            int fingerprintCount = classData.getCount();
            inputsProbabilityDataBuildingId.add(new ClassProbabilityData(classData.getOutputValue(), (double)fingerprintCount / trainingDataCount));

            for (int i = 0; i < WAPS_COUNT; i++) {
                double mean = 0;
                HashMap<Integer, Integer> inputValueRepetitions = new HashMap<>();
                for (int fingerprintId : classData.indices) {
                    int wapSignalIntensity = trainingData.get(fingerprintId).wapSignalIntensities[i];
                    mean += wapSignalIntensity;
                }
                mean /= fingerprintCount;

                // calculate variance
                double variance = 0;
                for (int fingerprintId : classData.indices) {
                    int wapSignalIntensity = trainingData.get(fingerprintId).wapSignalIntensities[i];
                    variance += Math.pow(wapSignalIntensity - mean, 2);
                }
                variance = variance / (fingerprintCount - 1) + 0.00001; // Avoid propigation -infinty when the probability is zero

                inputsProbabilityDataBuildingId.get(classId).addInputProbabilityData(new ClassProbabilityData.InputProbabilityData(mean, variance));
            }
        }
    }

    // if arrayList contains output value returns position number
    private static int getClassNumber(ArrayList<ClassData> classifiedFingerprints, int outputValue) {
        for (ClassData classData: classifiedFingerprints) {
            if (classData.getOutputValue() == outputValue) {
                return classifiedFingerprints.indexOf(classData);
            }
        }
        return -1;
    }

    private static int predictFloor(int validationFingerprintId) {
        Fingerprint validationFingerprint = validationData.get(validationFingerprintId);
        HashMap<Integer, Double> posteriorNumerators = new HashMap<>();
        for (ClassProbabilityData classProbabilityData : inputsProbabilityDataFloor) {
            double posteriorNumerator = classProbabilityData.getProbability();
            for (ClassProbabilityData.InputProbabilityData inputProbabilityData : classProbabilityData.inputsProbabilityData) {
                int wapId = classProbabilityData.inputsProbabilityData.indexOf(inputProbabilityData);
                if (!wapUsefulness[wapId]) {
                    continue;
                }
//                if (inputProbabilityData.getVariance() == 0) {
//                    continue;
//                }
                int wapSignalIntensity = validationFingerprint.wapSignalIntensities[wapId];
                double multiplier = 1 / (Math.sqrt(2 * Math.PI * Math.pow(inputProbabilityData.getVariance(), 2)) + 0.000001);
                double exponent = Math.exp(-Math.pow(wapSignalIntensity - inputProbabilityData.getMean(), 2) / (2 * Math.pow(inputProbabilityData.getVariance(), 2) + 0.000001));
                double posteriorNumeratorPerInput = multiplier * exponent;
                posteriorNumerator *= posteriorNumeratorPerInput;
            }
            posteriorNumerators.put(classProbabilityData.getOutputValue(), posteriorNumerator);
        }

        double posteriorNumeratorMax = 0;
        int result = 0;
        for (Map.Entry<Integer, Double> e : posteriorNumerators.entrySet()) {
            if (e.getValue() > posteriorNumeratorMax) {
                posteriorNumeratorMax = e.getValue();
                result = e.getKey();
            }
        }

        return result;
    }

    private static int predictBuildingId(int validationFingerprintId) {
        Fingerprint validationFingerprint = validationData.get(validationFingerprintId);
        HashMap<Integer, Double> posteriorNumerators = new HashMap<>();
        for (ClassProbabilityData classProbabilityData : inputsProbabilityDataBuildingId) {
            double posteriorNumerator = classProbabilityData.getProbability();
            for (ClassProbabilityData.InputProbabilityData inputProbabilityData : classProbabilityData.inputsProbabilityData) {
                int wapId = classProbabilityData.inputsProbabilityData.indexOf(inputProbabilityData);
                if (!wapUsefulness[wapId]) {
                    continue;
                }
//                if (inputProbabilityData.getVariance() == 0) {
//                    continue;
//                }
                int wapSignalIntensity = validationFingerprint.wapSignalIntensities[wapId];

                double multiplier = 1 / (Math.sqrt(2 * Math.PI * Math.pow(inputProbabilityData.getVariance(), 2)));
                double exponent = Math.exp(-Math.pow(wapSignalIntensity - inputProbabilityData.getMean(), 2) / (2 * Math.pow(inputProbabilityData.getVariance(), 2)));
                double posteriorNumeratorPerInput = multiplier * exponent;
                posteriorNumerator *= posteriorNumeratorPerInput;
            }
            posteriorNumerators.put(classProbabilityData.getOutputValue(), posteriorNumerator);
        }

        double posteriorNumeratorMax = 0;
        int result = 0;
        for (Map.Entry<Integer, Double> e : posteriorNumerators.entrySet()) {
            if (e.getValue() > posteriorNumeratorMax) {
                posteriorNumeratorMax = e.getValue();
                result = e.getKey();
            }
        }

        return result;
    }

    // predicts location data to all validation examples calculates absolute error and prints it to test.csv
    public static void doPrediction() throws FileNotFoundException, JSONException {
        long start = System.currentTimeMillis();

        PrintWriter pw = new PrintWriter(new File(OUTPUT_FILE));
        StringBuilder sb = new StringBuilder();
        double correctPredictionsCount = 0;

        sb.append("floorPredicted,buildingIdPredicted");
        sb.append('\n');

        for (int i = 0; i < validationDataCount; i++) {
            int floorPredicted = predictFloor(i);
            int floorActual = validationData.get(i).locationData.getFloor();
            int buildingIdPredicted = predictBuildingId(i);
            int buildingIdActual = validationData.get(i).locationData.getBuildingId();

            sb.append(floorPredicted);
            sb.append(',');
            sb.append(buildingIdPredicted);
            sb.append('\n');

            if (floorPredicted == floorActual && buildingIdPredicted == buildingIdActual) {
                correctPredictionsCount++;
            }
        }
        pw.write(sb.toString());
        pw.close();

        double successRate = correctPredictionsCount / validationDataCount * 100;

        long end = System.currentTimeMillis();
        addAlgorithmResult("NaiveBayes", successRate, end - start);
    }
}
