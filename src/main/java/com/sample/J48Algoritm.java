package com.sample;


import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.IOException;

/**
 * Created by Julee on 11.04.2017.
 */
public class J48Algoritm {
    private static Instances trainingData;
    private static Instances validationData;
    private static J48 j48;
    private static FilteredClassifier fc;

    public static void trainClassifier(String trainingDataFile, String validationDataFile) throws Exception {
        trainingData = prepareData(trainingDataFile);
        validationData = prepareData(validationDataFile);

        j48 = new J48();
        j48.setUnpruned(true); // using an unpruned J48
        // meta-classifier
        fc = new FilteredClassifier();
        fc.setClassifier(j48);
        // train
        fc.buildClassifier(trainingData);
    }

    private static Instances prepareData(String dataFile) throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(dataFile);
        Instances data = source.getDataSet();
        // setting class attribute if the data format does not provide this information
        // For example, the XRFF format saves the class attribute information as well
        if (data.classIndex() == -1) {
            data.setClassIndex(data.numAttributes() - 1);
        }

        return data;
    }

    public static void doPrediction() throws Exception {
        for (int i = 0; i < validationData.numInstances(); i++) {
            double pred = fc.classifyInstance(validationData.instance(i));
            System.out.print("ID: " + validationData.instance(i).value(0));
            System.out.print(", actual: " + validationData.classAttribute().value((int) validationData.instance(i).classValue()));
            System.out.println(", predicted: " + validationData.classAttribute().value((int) pred));
        }
    }
}
