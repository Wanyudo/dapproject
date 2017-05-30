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
import static com.sample.GlobalData.*;

/**
 * Created by Julee on 12.04.2017.
 */
public abstract class WekaAlgorithm {
    protected static Instances trainingInstancesClassNominalFloor;
    protected static Instances validationInstancesClassNominalFloor;

    protected static Instances trainingInstancesClassNominalBuildingId;
    protected static Instances validationInstancesClassNominalBuildingId;

    protected static final int CLASS_FLOOR = 0;
    protected static final int CLASS_BUILDING_ID = 1;

    public static void prepareFloorData(String trainingDataFile, String validationDataFile) throws Exception {
        Instances trainingInstancesUnfiltered = getInstances(trainingDataFile);
        Instances validationInstancesUnfiltered = getInstances(validationDataFile);

        /* Convert the last attribute to nominal */
        NumericToNominal convert = new NumericToNominal();
        convert.setAttributeIndices("last");
        convert.setInputFormat(validationInstancesUnfiltered);
        trainingInstancesClassNominalFloor = Filter.useFilter(trainingInstancesUnfiltered, convert);
        validationInstancesClassNominalFloor = Filter.useFilter(validationInstancesUnfiltered, convert);
    }

    public static void prepareBuildingIdData(String trainingDataFile, String validationDataFile) throws Exception {
        Instances trainingInstancesUnfiltered = getInstances(trainingDataFile);
        Instances validationInstancesUnfiltered = getInstances(validationDataFile);

        NumericToNominal convert = new NumericToNominal();
        convert.setAttributeIndices("last");
        convert.setInputFormat(validationInstancesUnfiltered);
        trainingInstancesClassNominalBuildingId = Filter.useFilter(trainingInstancesUnfiltered, convert);
        validationInstancesClassNominalBuildingId = Filter.useFilter(validationInstancesUnfiltered, convert);
    }

    private static Instances getInstances(String dataFile) throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(dataFile + OUTPUT_FILE_EXTENSION);
        Instances data = source.getDataSet();

        // The last attribute is to be predicted
        int numAttributes = data.numAttributes();
        data.setClassIndex(numAttributes - 1);

        return data;
    }

    private static double [] succesRates = new double[classCount];

    public static double doPrediction(Instances trainingInstancesFloor, Instances validationInstancesFloor, Instances trainingInstancesBuildingId, Instances validationInstancesBuildingId,
                                      AbstractClassifier classifierFloor, AbstractClassifier classifierBuildingId, String outputFileName)
            throws Exception {
        Evaluation evaluationFloor = new Evaluation(trainingInstancesFloor);
        evaluationFloor.evaluateModel(classifierFloor, validationInstancesFloor);
        ArrayList<Prediction> predictionsFloor = evaluationFloor.predictions();

        Evaluation evaluationBuildingId = new Evaluation(trainingInstancesBuildingId);
        evaluationBuildingId.evaluateModel(classifierBuildingId, validationInstancesBuildingId);
        ArrayList<Prediction> predictionsBuildingId = evaluationBuildingId.predictions();

        double correctPredictionsCount = 0;
        PrintWriter pw = new PrintWriter(new File(outputFileName + OUTPUT_FILE_EXTENSION));
        StringBuilder sb = new StringBuilder();

        sb.append("floorPredicted,buildingIdPredicted");
        sb.append('\n');

        for (int i = 0, iLim = predictionsFloor.size(); i < iLim; i ++) {
            double floorPredicted = predictionsFloor.get(i).predicted();
            double floorActual = predictionsFloor.get(i).actual();

            double buildingIdPredicted = predictionsBuildingId.get(i).predicted();
            double buildingIdActual = predictionsBuildingId.get(i).actual();

            sb.append(floorPredicted);
            sb.append(',');
            sb.append(buildingIdPredicted);
            sb.append('\n');

            if (floorPredicted == floorActual && buildingIdPredicted == buildingIdActual) {
                correctPredictionsCount++;
            }
        }
        pw.write(sb.toString());
        pw.close();

        return correctPredictionsCount / validationDataCount * 100;
    }
}
