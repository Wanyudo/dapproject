package com.sample;

import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

/**
 * Created by Julee on 12.04.2017.
 */
public class WekaAlgorithm {
    protected static Instances trainingInstances;
    protected static Instances validationInstances;

    protected static Instances trainingInstancesNominal;
    protected static Instances validationInstancesNominal;

    public static void prepareData(String trainingDataFile, String validationDataFile) throws Exception {
        trainingInstances = prepareData(trainingDataFile);
        validationInstances = prepareData(validationDataFile);

        NumericToNominal convert = new NumericToNominal();
        convert.setInputFormat(trainingInstances);

        trainingInstancesNominal = Filter.useFilter(trainingInstances, convert);
        validationInstancesNominal = Filter.useFilter(validationInstances, convert);
    }

    private static Instances prepareData(String dataFile) throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(dataFile);
        Instances data = source.getDataSet();

        // setting class attribute if the data format does not provide this information
        data.setClassIndex(data.numAttributes() - 1);

        return data;
    }
}
