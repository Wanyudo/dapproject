package com.sample;

import weka.classifiers.bayes.NaiveBayes;

import static com.sample.GlobalData.addAlgorithmResult;

/**
 * Created by Julee on 12.04.2017.
 */
public class WekaAlgorithmNaiveBayes extends WekaAlgorithm {
    static final String OUTPUT_FILE = "outputNaiveBayesWeka";

    // to predict time algorithm takes for training and prediction
    private static long start;
    private static long end;

    private static NaiveBayes classifierFloor, classifierBuildingId;

    public static void trainClassifier() throws Exception {
        start = System.currentTimeMillis();

        // train NaiveBayes
        classifierFloor = new NaiveBayes();
        classifierBuildingId = new NaiveBayes();
        classifierFloor.buildClassifier(trainingInstancesClassNominalFloor);
        classifierBuildingId.buildClassifier(trainingInstancesClassNominalBuildingId);
    }

    static void doPrediction() throws Exception {
        double successRate = doPrediction(trainingInstancesClassNominalFloor, validationInstancesClassNominalFloor, trainingInstancesClassNominalBuildingId, validationInstancesClassNominalBuildingId,
                classifierFloor, classifierBuildingId, OUTPUT_FILE);

        end = System.currentTimeMillis();
        addAlgorithmResult("WekaNaiveBayes", successRate, end - start);
    }
}
