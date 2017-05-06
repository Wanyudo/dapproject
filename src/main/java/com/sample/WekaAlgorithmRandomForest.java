package com.sample;

import weka.classifiers.trees.RandomForest;


public class WekaAlgorithmRandomForest extends WekaAlgorithm {
    static final String OUTPUT_FILE_FLOOR = "outputRandomForestWeka.csv";
    static final String OUTPUT_FILE_BUILDING_ID = "outputRandomForestWekaBuildingId.csv";

    private static RandomForest clasifier;

    public static void trainClassifier() throws Exception {
        clasifier = new RandomForest();
        clasifier.setMaxDepth(10); // maxDepth
        clasifier.setNumIterations(200); // numTrees
        clasifier.buildClassifier(trainingInstancesClassNominalFloor);
    }

    static void doPrediction() throws Exception {
        doPrediction(trainingInstancesClassNominalFloor, validationInstancesClassNominalFloor, clasifier, OUTPUT_FILE_FLOOR, 0);
        doPrediction(trainingInstancesClassNominalBuildingId, validationInstancesClassNominalBuildingId, clasifier, OUTPUT_FILE_FLOOR, 1);
    }
}
