package com.sample;

import weka.classifiers.bayes.NaiveBayes;

/**
 * Created by Julee on 12.04.2017.
 */
public class WekaAlgorithmNaiveBayes extends WekaAlgorithm {
    static final String OUTPUT_FILE_FLOOR = "outputNaiveBayesWeka.csv";
    static final String OUTPUT_FILE_BUILDING_ID = "outputNaiveBayesWekaBuildingId.csv";

    private static NaiveBayes clasifier;

    public static void trainClassifier() throws Exception {
        // train NaiveBayes
        clasifier = new NaiveBayes();
        clasifier.buildClassifier(trainingInstancesClassNominalFloor);
    }

    void doPrediction() throws Exception {
        doPrediction(trainingInstancesClassNominalFloor, validationInstancesClassNominalFloor, clasifier, OUTPUT_FILE_FLOOR, 0);
        doPrediction(trainingInstancesClassNominalBuildingId, validationInstancesClassNominalBuildingId, clasifier, OUTPUT_FILE_FLOOR, 1);
    }
}
