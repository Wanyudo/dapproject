package com.sample;

import java.util.ArrayList;

/**
 * Created by Julee on 12.04.2017.
 */
class ClassProbabilityData {
    /** Floor or Building ID */
    private int outputValue;
    private double probability;
    ArrayList<InputProbabilityData> inputsProbabilityData;

    static class InputProbabilityData {
        private double mean;
        private double variance;

        InputProbabilityData(double mean, double variance) {
            this.mean = mean;
            this.variance = variance;
        }

        double getMean() {
            return mean;
        }

        double getVariance() {
            return variance;
        }
    }

    int getOutputValue() {
        return outputValue;
    }

    double getProbability() {
        return probability;
    }

    ClassProbabilityData(int value, double probability) {
        this.outputValue = value;
        this.probability = probability;
        inputsProbabilityData = new ArrayList<>();
    }

    void addInputProbabilityData(InputProbabilityData inputProbabilityData) {
        inputsProbabilityData.add(inputProbabilityData);
    }
}