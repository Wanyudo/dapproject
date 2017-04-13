package com.sample;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.Prediction;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import static com.sample.CsvFileWriter.COMMA_DELIMITER;
import static com.sample.GlobalData.validationData;
import static com.sample.GlobalData.validationDataCount;
import static com.sample.WekaAlgorithm.*;

/**
 * Created by Julee on 12.04.2017.
 */
public class WekaAlgorithmNaiveBayes {
    public static final String OUTPUT_FILE_FLOOR = "outputNaiveBayesWeka.csv";

    private static NaiveBayes clasifier;

    public static void trainClassifier() throws Exception {
        // train NaiveBayes
        clasifier = new NaiveBayes();
        clasifier.buildClassifier(trainingInstancesNominal);
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
        sb.append("success rate: " + successRate + " %");
        sb.append('\n');
        pw.write(sb.toString());
        pw.close();
    }

}
