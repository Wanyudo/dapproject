package com.sample;

import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.trees.RandomForest;
import weka.core.Debug;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import static com.sample.CsvFileWriter.COMMA_DELIMITER;
import static com.sample.GlobalData.validationData;
import static com.sample.GlobalData.validationDataCount;

/**
 * Created by Julee on 12.04.2017.
 */
public class WekaAlgorithmRandomForest extends WekaAlgorithm {
    public static final String OUTPUT_FILE_FLOOR = "outputRandomForestWeka.csv";

    private static RandomForest clasifier;

    public static void trainClassifier() throws Exception {
        clasifier = new RandomForest();
        clasifier.setMaxDepth(10); // maxDepth
        clasifier.setNumIterations(100); // numTrees
        clasifier.buildClassifier(trainingInstances);
    }

    public static void doPrediction() throws Exception {
        Evaluation evaluation = new Evaluation(trainingInstances);
        evaluation.crossValidateModel(clasifier, validationInstances, 10, new Debug.Random(1));

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
