package com.sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static com.sample.GlobalData.*;
import static com.sample.KnnAlgorithm.sortNeighbors;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("WPS Demo");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


    public static void main(String[] args) {
        parseData(trainingData, "trainingData.csv");
        parseData(validationData, "validationData.csv");
        trainingDataCount = trainingData.size();
        validationDataCount = validationData.size();

        // set neighborList size equal to validation examples count
        for (int i = 0, iLim = validationData.size(); i < iLim; i++) {
            neighborList.add(null);
        }
        prepareKnnDataParallel();
        launch(args);
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
                for (int i = 0, iLim = WAPS_COUNT; i < iLim; i++) {
                    wapSignalIntensities[i] = Integer.parseInt(example[i]);
                }
                double longitude = Double.parseDouble(example[WAPS_COUNT + 1]);
                double latitude = Double.parseDouble(example[WAPS_COUNT + 2]);
                int floor = Integer.parseInt(example[WAPS_COUNT + 3]);
                int buildingId = Integer.parseInt(example[WAPS_COUNT + 4]);

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

    // sequential (very slow)
    private static void prepareKnnData() {
        for (Fingerprint validationFingerprint : validationData) {
            sortNeighbors(validationFingerprint);
        }
    }

    // parallel
    private static void prepareKnnDataParallel() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(validationData.size());
        Collection<Future<?>> futures = new LinkedList<Future<?>>();
        for (Fingerprint validationFingerprint : validationData) {
            futures.add(executor.submit(addSortNeighborsTask(validationFingerprint)));
        }
        // wait until all of the threads are finished
        for (Future<?> future:futures) {
            try {
                future.get();
            } catch (InterruptedException e) {

            } catch (ExecutionException e) {

            }
        }
    }

    public static WpsTask addSortNeighborsTask(final Fingerprint validationFingerprint) {
        final WpsTask task = new WpsTask() {
            @Override
            protected void doTask() {
                sortNeighbors(validationFingerprint);
            }
        };
        return task;
    }

}
