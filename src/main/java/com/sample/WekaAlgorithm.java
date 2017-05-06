package com.sample;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import static com.sample.CsvFileWriter.COMMA_DELIMITER;
import static com.sample.GlobalData.classCount;
import static com.sample.GlobalData.validationData;
import static com.sample.GlobalData.validationDataCount;

/**
 * Created by Julee on 12.04.2017.
 */
public abstract class WekaAlgorithm {
    protected static Instances trainingInstancesClassNominalFloor;
    protected static Instances validationInstancesClassNominalFloor;
    protected static Instances trainingInstancesAllNominalFloor;
    protected static Instances validationInstancesAllNominalFloor;

    protected static Instances trainingInstancesClassNominalBuildingId;
    protected static Instances validationInstancesClassNominalBuildingId;
    protected static Instances trainingInstancesAllNominalBuildingId;
    protected static Instances validationInstancesAllNominalBuildingId;

    public static void prepareFloorData(String trainingDataFile, String validationDataFile) throws Exception {
        Instances trainingInstancesUnfiltered = prepareFloorData(trainingDataFile);
        Instances validationInstancesUnfiltered = prepareFloorData(validationDataFile);

        NumericToNominal convert = new NumericToNominal();
        convert.setAttributeIndices("last");
        convert.setInputFormat(validationInstancesUnfiltered);
        trainingInstancesClassNominalFloor = Filter.useFilter(trainingInstancesUnfiltered, convert);
        validationInstancesClassNominalFloor = Filter.useFilter(validationInstancesUnfiltered, convert);

        NumericToNominal convert2 = new NumericToNominal();
        convert2.setAttributeIndices("first-last");
        convert2.setInputFormat(validationInstancesUnfiltered);
        trainingInstancesAllNominalFloor = Filter.useFilter(trainingInstancesUnfiltered, convert2);
        validationInstancesAllNominalFloor = Filter.useFilter(validationInstancesUnfiltered, convert2);
    }

    public static void prepareBuildingIdData(String trainingDataFile, String validationDataFile) throws Exception {
        Instances trainingInstancesUnfiltered = prepareFloorData(trainingDataFile);
        Instances validationInstancesUnfiltered = prepareFloorData(validationDataFile);

        NumericToNominal convert = new NumericToNominal();
        convert.setAttributeIndices("last");
        convert.setInputFormat(validationInstancesUnfiltered);
        trainingInstancesClassNominalBuildingId = Filter.useFilter(trainingInstancesUnfiltered, convert);
        validationInstancesClassNominalBuildingId = Filter.useFilter(validationInstancesUnfiltered, convert);

        NumericToNominal convert2 = new NumericToNominal();
        convert2.setAttributeIndices("first-last");
        convert2.setInputFormat(validationInstancesUnfiltered);
        trainingInstancesAllNominalBuildingId = Filter.useFilter(trainingInstancesUnfiltered, convert2);
        validationInstancesAllNominalBuildingId = Filter.useFilter(validationInstancesUnfiltered, convert2);
    }

    private static Instances prepareFloorData(String dataFile) throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(dataFile);
        Instances data = source.getDataSet();

        // setting class attribute if the data format does not provide this information
        int numAttributes = data.numAttributes();
        data.setClassIndex(numAttributes - 1);

        return data;
    }

    private static double [] succesRates = new double[classCount];

    public static void doPrediction(Instances trainingInstancesClassNominal, Instances validationInstancesClassNominal, AbstractClassifier clasifier, String outputFileName, int classIndex) throws Exception {
        Evaluation evaluation = new Evaluation(trainingInstancesClassNominal);
        evaluation.evaluateModel(clasifier, validationInstancesClassNominal);

        System.out.println(evaluation.toSummaryString("\nResults\n======\n", true));

        ArrayList<Prediction> predictions = evaluation.predictions();


        PrintWriter pw = new PrintWriter(new File(outputFileName));
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
        succesRates[classIndex] = successRate;

        pw.write(sb.toString());
        pw.close();
    }
}
