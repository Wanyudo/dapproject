package com.sample;


import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;

import java.io.*;
import java.util.ArrayList;

import static com.sample.CsvFileWriter.COMMA_DELIMITER;
import static com.sample.GlobalData.*;

/**
 * Created by Julee on 11.04.2017.
 */
public class WekaAlgorithmJ48 extends WekaAlgorithm{
    public static final String OUTPUT_FILE_FLOOR = "outputJ48Weka.csv";

    private static J48 clasifier;
    private static FilteredClassifier fc;

    public static void trainClassifier() throws Exception {
        clasifier = new J48();
        clasifier.setUnpruned(true); // using an unpruned J48
        // meta-classifier
        fc = new FilteredClassifier();
        fc.setClassifier(clasifier);

        // train
        fc.buildClassifier(trainingInstancesNominal);
    }

    public static void doPrediction() throws Exception {
        Evaluation evaluation = new Evaluation(trainingInstancesNominal);
        evaluation.evaluateModel(clasifier, validationInstancesNominal);

        System.out.println(evaluation.toSummaryString("\nResults\n======\n", true));
        ArrayList<Prediction> predictions = evaluation.predictions();


        PrintWriter pw = new PrintWriter(new File(OUTPUT_FILE_FLOOR));
        StringBuilder sb = new StringBuilder();

        int correctPredictionsCount = 0;

        for (Prediction prediction : predictions) {
            int i = predictions.indexOf(prediction);

            double floor = prediction.predicted();
            sb.append(prediction.actual());
            sb.append(COMMA_DELIMITER);
            sb.append(floor);
            sb.append('\n');

            if (validationData.get(i).locationData.getFloor() == floor) {
                correctPredictionsCount++;
            }
        }

        double successRate = (double) correctPredictionsCount / validationDataCount * 100;
        sb.append("success rate: " + successRate + "%");
        sb.append('\n');
        pw.write(sb.toString());
        pw.close();
    }
}
