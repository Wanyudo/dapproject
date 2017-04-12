package com.sample;

import java.util.ArrayList;

/**
 * Created by Julee on 12.04.2017.
 */
class ClassProbabilityData {
    private int outputValue;
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

    ClassProbabilityData(int value, ArrayList<InputProbabilityData> xParams) {
        this.outputValue = value;
        this.inputsProbabilityData = xParams;
    }

    ClassProbabilityData(int value) {
        this.outputValue = value;
        inputsProbabilityData = new ArrayList<InputProbabilityData>();
    }

    void addInputProbabilityData(InputProbabilityData inputProbabilityData) {
        inputsProbabilityData.add(inputProbabilityData);
    }
}
