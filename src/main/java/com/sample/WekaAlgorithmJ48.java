package com.sample;


import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import static com.sample.CsvFileWriter.COMMA_DELIMITER;
import static com.sample.GlobalData.OUTPUT_FILE_EXTENSION;
import static com.sample.GlobalData.addAlgorithmResult;
import static com.sample.GlobalData.addJ48Result;


public class WekaAlgorithmJ48 extends WekaAlgorithm{
    static final String OUTPUT_FILE = "outputJ48Weka";
    static String OUTPUT_FILE_PREFIX = "";

    private static J48 classifierFloor, classifierBuildingId;
    private static FilteredClassifier filteredClassifierFloor, filteredClassifierBuildingId;

    // to predict time algorithm takes for training and prediction
    private static long start;
    private static long end;

    public static void trainClassifier(boolean unpruned, int minNumObj) throws Exception {
        start = System.currentTimeMillis();

        classifierFloor = new J48();
        classifierBuildingId = new J48();
        classifierFloor.setUnpruned(unpruned); // using an unpruned J48
        classifierBuildingId.setUnpruned(unpruned); // using an unpruned J48

        if (minNumObj > 0) {
            classifierFloor.setMinNumObj(minNumObj);
            classifierBuildingId.setMinNumObj(minNumObj);
        }

        OUTPUT_FILE_PREFIX = "";
        if (unpruned) {
            OUTPUT_FILE_PREFIX += "_unpruned";
        } else {
            OUTPUT_FILE_PREFIX += "_pruned";
        }
        OUTPUT_FILE_PREFIX += "_minNumObj_" + minNumObj;

        // train
        classifierFloor.buildClassifier(trainingInstancesClassNominalFloor);
        classifierBuildingId.buildClassifier(trainingInstancesClassNominalBuildingId);
    }

    static void doPrediction() throws Exception {
        double successRate = doPrediction(trainingInstancesClassNominalFloor, validationInstancesClassNominalFloor, trainingInstancesClassNominalBuildingId, validationInstancesClassNominalBuildingId,
                classifierFloor, classifierBuildingId, OUTPUT_FILE + OUTPUT_FILE_PREFIX);

        end = System.currentTimeMillis();
        addJ48Result(classifierFloor.getUnpruned(), classifierFloor.getMinNumObj(), successRate, end - start);
    }
}
