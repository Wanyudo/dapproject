package com.sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.Pane;
import org.json.JSONException;

import java.io.FileNotFoundException;

import static com.sample.NaiveBayesAlgorithm.prepareNaiveBayesData;

/**
 * GUI controller
 */
public class Controller {

    @FXML
    private LineChart<Integer, Integer> wpsLineChart;

    @FXML
    private ComboBox<String> comboBoxAlgorithms;

    @FXML
    private Button btnMakePredictions;

    @FXML
    private RadioButton rbDefault;

    @FXML
    private RadioButton rbDefaultWeigthed;

    @FXML
    private RadioButton rbVoting;

    @FXML
    private Label algParam;

    @FXML
    private Label algParam2;

    @FXML
    private CheckBox chkbxJ48;

    @FXML
    private Pane thisiska;

    @FXML
    private Spinner Spinka;

    @FXML
    private Spinner Spinka2;

    private static boolean knnDataPrepared = false;

    private static boolean nbDataPrepared = false;

    @FXML
    public void initialize() {
        Spinka.setDisable(true);
        Spinka.setVisible(false);
        Spinka2.setDisable(true);
        Spinka2.setVisible(false);
        kNNControlsHide();
        chkbxJ48.setVisible(false);
        chkbxJ48.setDisable(true);
        algParam2.setVisible(false);

//        wpsLineChart.getData().add(generateData());
//        wpsLineChart.setLegendSide(Side.RIGHT);
//
//        isShowKNN.setOnMouseClicked((event) -> {
//            if (isShowKNN.isSelected()) {
//                wpsLineChart.getData().add(generateData());
//            } else {
//                wpsLineChart.getData().remove(0);
//            }
//        });
//
//        isShowBayes.setOnMouseClicked((event) -> {
//            if (isShowKNN.isSelected()) {
//                wpsLineChart.getData().add(generateData2());
//            } else {
//                wpsLineChart.getData().remove(1);
//            }
//        });

        // listen for changes to the algorithm combo box selection and update the displayed algorithm image accordingly.
        comboBoxAlgorithms.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> selected, String oldAlgorithm, String newAlgorithm) {
                if (oldAlgorithm != null) {
                    switch (oldAlgorithm) {
                        case "KNN":
                            kNNControlsHide();
                            break;
                        case "Naive Bayes":
                            break;
                        case "Naive Bayes (Weka)":
                            break;
                        case "KNN (Weka)":
                            break;
                        case "J48 (Weka)":
                            j48ControlsHide();
                            break;
                        case "Random Forest (Weka)":
                            rfControlsHide();
                            break;
                    }
                }
                if (newAlgorithm != null) {
                    switch (newAlgorithm) {
                        case "KNN":
                            kNNControlsShow();
                            break;
                        case "Naive Bayes":
                            break;
                        case "Naive Bayes (Weka)":
                            break;
                        case "KNN (Weka)":
                            break;
                        case "J48 (Weka)":
                            j48ControlsShow();
                            break;
                        case "Random Forest (Weka)":
                            rfControlsShow();
                            break;
                    }
                }
            }
        });
    }

    /**
     * General Make predictions big method.
     */
    @FXML
    public void makePredictions() throws Exception {
        new Thread(() -> {
            try {
                thisiska.setDisable(true);
                predict();
                thisiska.setDisable(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void predict() throws Exception {
        // KNN method.
        if ("KNN".equals(comboBoxAlgorithms.getValue())) {
            int k = Integer.parseInt(Spinka.getValue().toString());
            kNNMethod(k);
        }

        //Naive Bayes method.
        if ("Naive Bayes".equals(comboBoxAlgorithms.getValue())) {
            naiveBayesMethod();
        }

        //Weka Naive Bayes method.
        if ("Naive Bayes (Weka)".equals(comboBoxAlgorithms.getValue())) {
            wekaNaiveBayesMethod();
        }

        //Weka Naive Bayes method.
        if ("J48 (Weka)".equals(comboBoxAlgorithms.getValue())) {
            boolean unpurned = chkbxJ48.isSelected();
            int minObj = Integer.parseInt(Spinka.getValue().toString());
            wekaJ48Method(unpurned, minObj);
        }

        //Weka Random Forest method.
        if ("Random Forest (Weka)".equals(comboBoxAlgorithms.getValue())) {
            int maxDepth = Integer.parseInt(Spinka.getValue().toString());
            int numIter = Integer.parseInt(Spinka2.getValue().toString());
            wekaRFMethod(maxDepth, numIter);
        }
    }

    private void kNNMethod(int pK) throws Exception {
        // use KNN classifier
        if (!knnDataPrepared) {
            KnnAlgorithm.prepareKnnDataParallel();
            knnDataPrepared = true;
        }
        KnnAlgorithm.doPrediction(pK, getRB());
    }

    /**
     * Method for KNN: checks which of 3.
     */
    private int getRB() throws Exception {
        if (rbDefault.isSelected()) return 0;
        if (rbDefaultWeigthed.isSelected()) return 1;
        if (rbVoting.isSelected()) return 2;
        throw new Exception();
    }

    /**
     * Use Naive Bayes classifier:
     * true - floor
     * false - building
     */
    private void naiveBayesMethod() throws FileNotFoundException, JSONException {
        if (!nbDataPrepared) {
            prepareNaiveBayesData();
            nbDataPrepared = true;
        }
        NaiveBayesAlgorithm.doPrediction();
    }

    /**
     * Use Weka Naive Bayes classifier:
     */
    private void wekaNaiveBayesMethod() throws Exception {
        WekaAlgorithmNaiveBayes.trainClassifier();
        WekaAlgorithmNaiveBayes.doPrediction();
    }

    /**
     * Use Weka J48 classifier:
     */
    private void wekaJ48Method(boolean unPurned, int minObj) throws Exception {
        WekaAlgorithmJ48.trainClassifier(unPurned, minObj);
        WekaAlgorithmJ48.doPrediction();
    }

    /**
     * use Random Forest classifier from Weka
     */
    private void wekaRFMethod(int maxDepth, int numIter) throws Exception {
        WekaAlgorithmRandomForest.trainClassifier(maxDepth, numIter);
        WekaAlgorithmRandomForest.doPrediction();
    }

    @FXML
    /**
     * Deselects other radiobutton
     */
    public void selectDefaultRb(){
        rbDefaultWeigthed.setSelected(false);
        rbDefault.setSelected(true);
        rbVoting.setSelected(false);
    }

    @FXML
    /**
     * Deselects other radiobutton
     */
    public void selectDefaultWeightedRb(){
        rbDefault.setSelected(false);
        rbVoting.setSelected(false);
        rbDefaultWeigthed.setSelected(true);
    }

    @FXML
    /**
     * Deselects other radiobutton
     */
    public void selectVotingRb(){
        rbDefault.setSelected(false);
        rbVoting.setSelected(true);
        rbDefaultWeigthed.setSelected(false);
    }

    /**
     * Initialize Radio Buttons for KNN algorithm
     */
    private void kNNControlsHide(){
        algParam.setVisible(false);
        Spinka.setVisible(false);
        Spinka2.setVisible(false);
        Spinka.setDisable(true);
        Spinka2.setDisable(true);
        hideRadioButtons();
    }

    private void kNNControlsShow(){
        Spinka.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,19937, 2));
        Spinka.setVisible(true);
        Spinka.setDisable(false);
        rbDefault.setText("Default");
        rbDefaultWeigthed.setText("Default Weighted");
        rbVoting.setText("Voting");
        rbVoting.setDisable(false);
        rbVoting.setVisible(true);

        //Set Label text next to Spinner
        algParam.setVisible(true);
        algParam.setText("K: ");

        showRadioButtons();
    }

    /**
     * Initialize Controls for Weka J48
     */
    private void j48ControlsHide(){
        Spinka.setVisible(false);
        Spinka.setDisable(true);
        //Set Label text next to Spinner
        algParam.setVisible(false);
        chkbxJ48.setVisible(false);
        chkbxJ48.setDisable(true);
    }

    private void j48ControlsShow(){
        Spinka.setVisible(true);
        Spinka.setDisable(false);
        Spinka.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2,10, 2));
        chkbxJ48.setVisible(true);
        chkbxJ48.setDisable(false);
        chkbxJ48.setSelected(true);
        //Set Label text next to Spinner
        algParam.setVisible(true);
        algParam.setText("Min. objects: ");
    }

    /**
     * Initialize Controls for Random Forest
     */
    private void rfControlsHide(){
        Spinka.setVisible(false);
        Spinka.setDisable(true);
        Spinka2.setVisible(false);
        Spinka2.setDisable(true);
        //Set Label text next to Spinner
        algParam.setVisible(false);
        algParam2.setVisible(false);
    }

    private void rfControlsShow(){
        Spinka.setVisible(true);
        Spinka.setDisable(false);
        Spinka2.setVisible(true);
        Spinka2.setDisable(false);
        Spinka.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0,10, 1));
        Spinka2.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10,100, 10));

        //Set Label text next to Spinner
        algParam.setVisible(true);
        algParam.setText("Max. Depth: ");
        algParam2.setVisible(true);
        algParam2.setText("Number of iterations: ");
    }

    /**
     * RadioButtons show/hide
     */
    private void showRadioButtons(){
        //Default selections for radioButtons
        rbDefault.setSelected(true);
        rbDefaultWeigthed.setSelected(false);
        rbDefault.setVisible(true);
        rbDefault.setDisable(false);
        rbDefaultWeigthed.setVisible(true);
        rbDefaultWeigthed.setDisable(false);
    }

    private void hideRadioButtons(){
        rbDefault.setText("N/A");
        rbDefaultWeigthed.setText("N/A");
        rbDefault.setVisible(false);
        rbDefault.setDisable(true);
        rbDefaultWeigthed.setVisible(false);
        rbVoting.setVisible(false);
        rbDefaultWeigthed.setDisable(true);
        rbVoting.setDisable(true);
    }

//    private XYChart.Series generateData(){
//        XYChart.Series series = new XYChart.Series();
//        series.setName("KNN");
//        series.getData().add(new XYChart.Data(15, 23));
//        series.getData().add(new XYChart.Data(2, 14));
//        series.getData().add(new XYChart.Data(3, 15));
//        series.getData().add(new XYChart.Data(4, 24));
//        series.getData().add(new XYChart.Data(5, 34));
//        series.getData().add(new XYChart.Data(6, 36));
//        series.getData().add(new XYChart.Data(7, 22));
//        series.getData().add(new XYChart.Data(8, 45));
//        series.getData().add(new XYChart.Data(9, 43));
//        series.getData().add(new XYChart.Data(10, 17));
//        series.getData().add(new XYChart.Data(11, 29));
//        series.getData().add(new XYChart.Data(12, 25));
//        return series;
//    }
//
//    private XYChart.Series generateData2(){
//        XYChart.Series series = new XYChart.Series();
//        series.setName("KNN");
//        series.getData().add(new XYChart.Data(15, 23));
//        series.getData().add(new XYChart.Data(2, 14));
//        series.getData().add(new XYChart.Data(3, 21));
//        series.getData().add(new XYChart.Data(4, 24));
//        series.getData().add(new XYChart.Data(5, 34));
//        series.getData().add(new XYChart.Data(6, 36));
//        series.getData().add(new XYChart.Data(7, 22));
//        series.getData().add(new XYChart.Data(8, 41));
//        series.getData().add(new XYChart.Data(9, 40));
//        series.getData().add(new XYChart.Data(10, 24));
//        series.getData().add(new XYChart.Data(11, 29));
//        series.getData().add(new XYChart.Data(12, 25));
//        return series;
//    }
}
