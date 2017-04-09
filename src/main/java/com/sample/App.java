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
        double distanceMin = 1000000;
        for (Fingerprint validationFingerprint : validationData) {
            ArrayList<Integer> neighbors = new ArrayList<Integer>();
            for (Fingerprint trainingFingerprint : trainingData)  {
                // calculate the Euclidean distance
                double distance = 0;
                for (int i = 0; i < WAPS_COUNT; i++) {
                    distance += Math.pow(validationFingerprint.wapSignalIntensities[i] - trainingFingerprint.wapSignalIntensities[i], 2);
                }
                distance = Math.sqrt(distance);

                // place trainingFingerprint id to neighbor list ascending by distance
                boolean placed = false;
                for (int i = 0, iLim = neighbors.size(); i < iLim; i++) {
                    if (distance > distanceMin) continue;
                    neighbors.add(i, trainingData.indexOf(trainingFingerprint));
                    distanceMin = distance;
                    placed = true;
                    break;
                }
                // add the farthest trainingFingerprint to the end of the list
                if (!placed) {
                    neighbors.add(trainingData.indexOf(trainingFingerprint));
                }
            }
            neighborList.add(neighbors);
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
                ArrayList<Integer> neighbors = new ArrayList<Integer>();
                double distanceMin = 1000000;
                for (Fingerprint trainingFingerprint : trainingData)  {
                    // calculate the Euclidean distance
                    double distance = 0;
                    for (int i = 0; i < WAPS_COUNT; i++) {
                        distance += Math.pow(validationFingerprint.wapSignalIntensities[i] - trainingFingerprint.wapSignalIntensities[i], 2);
                    }
                    distance = Math.sqrt(distance);

                    // place trainingFingerprint id to neighbor list ascending by distance
                    boolean placed = false;
                    for (int i = 0, iLim = neighbors.size(); i < iLim; i++) {
                        if (distance > distanceMin) continue;
                        neighbors.add(i, trainingData.indexOf(trainingFingerprint));
                        distanceMin = distance;
                        placed = true;
                        break;
                    }
                    // add the farthest trainingFingerprint to the end of the list
                    if (!placed) {
                        neighbors.add(trainingData.indexOf(trainingFingerprint));
                    }
                }
                addNeighbors(validationData.indexOf(validationFingerprint), neighbors);
            }
        };
        return task;
    }

    private static synchronized void addNeighbors(int validationFingerprintId, ArrayList<Integer> neighbors) {
        neighborList.set(validationFingerprintId, neighbors);
    }

}
