package com.sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.sample.GlobalData.*;
import static com.sample.PublicMethods.addItemToRepetitions;
import static com.sample.PublicMethods.calculateProbabilities;

/**
 * Created by Julee on 11.04.2017.
 */

public class NaiveBayesAlgorithm {
    private static ArrayList<ClassProbabilityData> inputsProbabilityData; // stores mean & variance for wap grouped by classes
    public static final String OUTPUT_FILE_FLOOR = "outputNaiveBayes.csv";


    public static void prepareNaiveBayesData() {
        ArrayList<ClassData> classifiedFingerprints = new ArrayList<ClassData>(); // keeps class output value and number of fingerprints from training data

        // group fingerprints from trainingData by floor value
        for (Fingerprint fingerprint : trainingData) {
            int floor = fingerprint.locationData.getFloor();
            int fingerprintId = trainingData.indexOf(fingerprint);
            int classDataId = getClassNumber(classifiedFingerprints, floor);
            if (classDataId == -1) {
                classifiedFingerprints.add(new ClassData(floor, fingerprintId));
            } else {
                classifiedFingerprints.get(classDataId).addFingerprintId(fingerprintId);
            }
        }

        // calculate mean and variance
        inputsProbabilityData = new ArrayList<ClassProbabilityData>();
        for (ClassData classData : classifiedFingerprints) {
            int classId = classifiedFingerprints.indexOf(classData);
            int fingerprintCount = classData.getCount();
            inputsProbabilityData.add(new ClassProbabilityData(classData.getOutputValue(), (double)fingerprintCount / trainingDataCount));

            for (int i = 0; i < WAPS_COUNT; i++) {
                double mean = 0;
                HashMap<Integer, Integer> inputValueRepetitions = new HashMap<Integer, Integer>();
                // calculate mean and group fingerprints by wap signal intensity
                for (int j = 0; j < fingerprintCount; j++) {
                    int wapSignalIntensity = trainingData.get(classData.getFingerprintId(j)).wapSignalIntensities[i];
                    mean += wapSignalIntensity;
                    addItemToRepetitions(wapSignalIntensity, inputValueRepetitions);
                }
                mean /= fingerprintCount;
                ArrayList<outputProbability> inputValueProbabilities = (calculateProbabilities(inputValueRepetitions, fingerprintCount));

                // calculate variance
                double mX = 0, mX2 = 0;
                double variance = 0;
                for (int j = 0, jLim = inputValueProbabilities.size(); j < jLim; j++) {
                    outputProbability xValueProbability = inputValueProbabilities.get(j);
//                    mX +=  xValueProbability.probability * xValueProbability.outputValue;
//                    mX2 +=  xValueProbability.probability * xValueProbability.outputValue * xValueProbability.outputValue;
                    variance += Math.pow(xValueProbability.outputValue - mean, 2);
                }

                variance = variance / fingerprintCount + 0.00000001;
//                mean = mX;
//                double variance = mX2 - mX * mX + 0.00000001/*Double.MIN_VALUE*/;

                inputsProbabilityData.get(classId).addInputProbabilityData(new ClassProbabilityData.InputProbabilityData(mean, variance));
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
        HashMap<Integer, Double> posteriorNumerators = new HashMap<Integer, Double>();
        for (ClassProbabilityData classProbabilityData : inputsProbabilityData) {
            double posteriorNumerator = classProbabilityData.getProbability();
            for (ClassProbabilityData.InputProbabilityData inputProbabilityData : classProbabilityData.inputsProbabilityData) {
                int wapId = classProbabilityData.inputsProbabilityData.indexOf(inputProbabilityData);
                int wapSignalIntensity = validationFingerprint.wapSignalIntensities[wapId];
                double multiplier = 1 / Math.sqrt(2 * Math.PI * Math.pow(inputProbabilityData.getVariance(), 2));
                double posteriorNumeratorPerInput = multiplier * Math.exp(-Math.pow(wapSignalIntensity - inputProbabilityData.getMean(), 2) / (2 * Math.pow(inputProbabilityData.getVariance(), 2)));
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
    public static void doPrediction() throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new File(OUTPUT_FILE_FLOOR));
        StringBuilder sb = new StringBuilder();

        int correctPredictionsCount = 0;

        for (int i = 0; i < validationDataCount; i++) {
            int floor = predictFloor(i);
            sb.append(floor);
            sb.append('\n');

            if (validationData.get(i).locationData.getFloor() == floor) {
                correctPredictionsCount++;
            }
        }

        double successRate = (double) correctPredictionsCount / validationDataCount * 100;
        sb.append("success rate:" + successRate + " %");
        sb.append('\n');
        pw.write(sb.toString());
        pw.close();
    }
}
