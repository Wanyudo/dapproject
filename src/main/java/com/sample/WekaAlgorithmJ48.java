package com.sample;


import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;


public class WekaAlgorithmJ48 extends WekaAlgorithm{
    static final String OUTPUT_FILE_FLOOR = "outputJ48WekaFloor.csv";
    static final String OUTPUT_FILE_BUILDING_ID = "outputJ48WekaBuildingId.csv";

    private static J48 clasifier;
    private static FilteredClassifier fc;

    public static void trainClassifier() throws Exception {
        clasifier = new J48();
        //clasifier.setUnpruned(true); // using an unpruned J48
        clasifier.setMinNumObj(10); // using an unpruned J48
        // meta-classifier
        fc = new FilteredClassifier();
        fc.setClassifier(clasifier);

        // train
        fc.buildClassifier(trainingInstancesAllNominalFloor);
    }

    void doPrediction() throws Exception {
        doPrediction(trainingInstancesAllNominalFloor, validationInstancesAllNominalFloor, clasifier, OUTPUT_FILE_FLOOR, 0);
        doPrediction(trainingInstancesAllNominalBuildingId, validationInstancesAllNominalBuildingId, clasifier, OUTPUT_FILE_FLOOR, 1);
    }
}
