package com.sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static com.sample.CsvFileWriter.COMMA_DELIMITER;
import static com.sample.CsvFileWriter.writeCsvFileClassAndAttributes;
import static com.sample.GlobalData.*;
import static com.sample.KnnAlgorithm.KNN_DEFAULT;
import static com.sample.KnnAlgorithm.KNN_VOTING;
import static com.sample.KnnAlgorithm.KNN_WEIGHTING;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("WPS Demo");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) throws Exception {
        parseAlgorithmResultFile();

        parseData(trainingData, TRAINING_DATA_FILE);
        parseData(validationData, VAILDATION_DATA_FILE);
        trainingDataCount = trainingData.size();
        validationDataCount = validationData.size();

        int cantBeIgnored = 0;
        for (int i = 0; i < WAPS_COUNT; i++) {
            int rssi = trainingData.get(0).wapSignalIntensities[i];
            for (Fingerprint fingerprint : trainingData) {
                wapUsefulness[i] = false;
                if (fingerprint.wapSignalIntensities[i] != rssi) {
                    cantBeIgnored++;
                    wapUsefulness[i] = true;
                    break;
                }
            }
        }

        // Prepare data for Weka algorithms
        File f = new File(TRAINING_DATA_FILE_FLOOR + OUTPUT_FILE_EXTENSION);
        if (!f.exists() || f.isDirectory()) {
            f = new File(TRAINING_DATA_FILE_BUILDING_ID + OUTPUT_FILE_EXTENSION);
            if (!f.exists() || f.isDirectory()) {
                f = new File(VAILDATION_DATA_FILE_FLOOR + OUTPUT_FILE_EXTENSION);
                if (!f.exists() || f.isDirectory()) {
                    f = new File(VAILDATION_DATA_FILE_BUILDING_ID + OUTPUT_FILE_EXTENSION);
                    if (!f.exists() || f.isDirectory()) {
                        prepareFiles();
                    }
                }
            }
        }
        WekaAlgorithm.prepareFloorData(TRAINING_DATA_FILE_FLOOR, VAILDATION_DATA_FILE_FLOOR);
        WekaAlgorithm.prepareBuildingIdData(TRAINING_DATA_FILE_BUILDING_ID, VAILDATION_DATA_FILE_BUILDING_ID);

        /*// use KNN classifier
        KnnAlgorithm.prepareKnnDataParallel();
        for (int k = 1; k <= 20; k++) {
            KnnAlgorithm.doPrediction(k, KNN_DEFAULT);
        }
        for (int k = 1; k <= 20; k++) {
            KnnAlgorithm.doPrediction(k, KNN_WEIGHTING);
        }
        for (int k = 1; k <= 20; k++) {
            KnnAlgorithm.doPrediction(k, KNN_VOTING);
        }

        NaiveBayesAlgorithm.prepareNaiveBayesData();
        NaiveBayesAlgorithm.doPrediction();

        WekaAlgorithmNaiveBayes.trainClassifier();
        WekaAlgorithmNaiveBayes.doPrediction();

        WekaAlgorithmJ48.trainClassifier(false, 2);
        WekaAlgorithmJ48.doPrediction();
        WekaAlgorithmJ48.trainClassifier(false, 10);
        WekaAlgorithmJ48.doPrediction();
        WekaAlgorithmJ48.trainClassifier(true, 2);
        WekaAlgorithmJ48.doPrediction();
        WekaAlgorithmJ48.trainClassifier(true, 10);
        WekaAlgorithmJ48.doPrediction();

        WekaAlgorithmRandomForest.trainClassifier(0,100);
        WekaAlgorithmRandomForest.doPrediction();
        WekaAlgorithmRandomForest.trainClassifier(0,200);
        WekaAlgorithmRandomForest.doPrediction();
        WekaAlgorithmRandomForest.trainClassifier(10,100);
        WekaAlgorithmRandomForest.doPrediction();
        WekaAlgorithmRandomForest.trainClassifier(10,200);
        WekaAlgorithmRandomForest.doPrediction();*/

        launch(args);
    }

    private static void prepareFiles() {
        parseHeaders();
        writeCsvFileClassAndAttributes(TRAINING_DATA_FILE_FLOOR, trainingData, FINGERPRINT_HEADER_FLOOR, true);
        writeCsvFileClassAndAttributes(VAILDATION_DATA_FILE_FLOOR, validationData, FINGERPRINT_HEADER_FLOOR, true);
        writeCsvFileClassAndAttributes(TRAINING_DATA_FILE_BUILDING_ID, trainingData, FINGERPRINT_HEADER_FLOOR, false);
        writeCsvFileClassAndAttributes(VAILDATION_DATA_FILE_BUILDING_ID, validationData, FINGERPRINT_HEADER_FLOOR, false);
    }

    private static void parseData(List<Fingerprint> data, String dataFile) {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {
            InputStream is = App.class.getClassLoader().getResourceAsStream(dataFile);
            br = new BufferedReader(new InputStreamReader(is));

            line = br.readLine();

            while ((line = br.readLine()) != null) {
                String[] example = line.split(cvsSplitBy); // use comma as separator

                int [] wapSignalIntensities = new int [WAPS_COUNT];
                for (int i = 0; i < WAPS_COUNT; i++) {
                    int signal = Integer.parseInt(example[i]);
                    if (signal == 100) signal = -104;
                    wapSignalIntensities[i] = signal;
                }
                double longitude = Double.parseDouble(example[WAPS_COUNT]);
                double latitude = Double.parseDouble(example[WAPS_COUNT + 1]);
                int floor = Integer.parseInt(example[WAPS_COUNT + 2]);
                int buildingId = Integer.parseInt(example[WAPS_COUNT + 3]);

                data.add(new Fingerprint(wapSignalIntensities, longitude, latitude, floor, buildingId));
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Info: FileNotFoundException", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Info: IOException", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Info: IOException", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private static void parseHeaders() {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {
            InputStream is = App.class.getClassLoader().getResourceAsStream(VAILDATION_DATA_FILE);
            br = new BufferedReader(new InputStreamReader(is));

            line = br.readLine();
            String[] header = line.split(cvsSplitBy);
            for (int i = 0; i < WAPS_COUNT; i++) {
                FINGERPRINT_HEADER_FLOOR += header[i] + COMMA_DELIMITER;
                FINGERPRINT_HEADER_BUILDING_ID += header[i] + COMMA_DELIMITER;
            }
            FINGERPRINT_HEADER_FLOOR += header[WAPS_COUNT + 2];
            FINGERPRINT_HEADER_BUILDING_ID += header[WAPS_COUNT + 3];

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Info: FileNotFoundException", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Info: IOException", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Info: IOException", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
