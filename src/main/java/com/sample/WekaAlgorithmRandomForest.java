package com.sample;

import weka.classifiers.trees.RandomForest;

import static com.sample.GlobalData.addRandomForestResult;


public class WekaAlgorithmRandomForest extends WekaAlgorithm {
    static final String OUTPUT_FILE = "outputRandomForestWeka";
    static String OUTPUT_FILE_PREFIX = "";

    // to predict time algorithm takes for training and prediction
    private static long start;
    private static long end;

    private static RandomForest classifierFloor, classifierBuildingId;

    public static void trainClassifier(int maxDepth, int numIterations) throws Exception {
        start = System.currentTimeMillis();

        classifierFloor = new RandomForest();
        classifierBuildingId = new RandomForest();
        if (maxDepth > 0) {
            classifierFloor.setMaxDepth(maxDepth); // maxDepth
            classifierBuildingId.setMaxDepth(maxDepth); // maxDepth
        }
        if (numIterations > 0) {
            classifierFloor.setNumIterations(numIterations); // numTrees
            classifierBuildingId.setNumIterations(numIterations); // numTrees
        }

        OUTPUT_FILE_PREFIX = "maxDepth_" + maxDepth;
        OUTPUT_FILE_PREFIX += "_numIterations_" + numIterations;

        classifierFloor.buildClassifier(trainingInstancesClassNominalFloor);
        classifierBuildingId.buildClassifier(trainingInstancesClassNominalBuildingId);
    }

    static void doPrediction() throws Exception {
        double successRate = doPrediction(trainingInstancesClassNominalFloor, validationInstancesClassNominalFloor, trainingInstancesClassNominalBuildingId, validationInstancesClassNominalBuildingId,
                classifierFloor, classifierBuildingId, OUTPUT_FILE + OUTPUT_FILE_PREFIX);

        end = System.currentTimeMillis();
        addRandomForestResult(classifierFloor.getMaxDepth(), classifierFloor.getNumIterations(), successRate, end - start);
    }
}
