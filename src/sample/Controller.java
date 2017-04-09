package sample;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

/**
 * Created by Julee on 03.04.2017.
 */
public class Controller {

    @FXML
    private Label kValueLabel;
    @FXML
    private Slider kValueSlider;

    public Controller() {
    }

    @FXML
    protected void initialize() {
        kValueLabel.textProperty().bind(
                Bindings.format(
                        "%.0f",
                        (kValueSlider.valueProperty())
                )
        );


    }
}
