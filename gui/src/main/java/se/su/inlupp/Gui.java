package se.su.inlupp;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.FlowPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Gui extends Application {


  private final Button findPathButton = new Button ("Find Path");
  private final Button showConnectionButton = new Button ("Show Connection");
  private final Button newPlaceButton = new Button ("New Place");
  private final Button newConnectionButton = new Button ("New Connection");
  private final Button changeConnectionButton = new Button ("Change Connection");


  private final ImageView mapView = new ImageView();

  @Override
  public void start(Stage stage) {
    //Graph<String> graph = new ListGraph<>(); //Kommer att användas senare

    Menu fileMenu = new Menu("File");

    MenuItem newMapItem = new MenuItem("New Map");
    MenuItem openItem = new MenuItem ("Open");
    MenuItem saveItem = new MenuItem("Save");
    MenuItem saveImageItem = new MenuItem("Save Image");
    MenuItem exitItem = new MenuItem("Exit");

    fileMenu.getItems().addAll(newMapItem, openItem, saveItem, saveImageItem, exitItem);

    MenuBar menuBar = new MenuBar(fileMenu);

    disableAllButtons();

    FlowPane buttonsPane = new FlowPane(Orientation.HORIZONTAL, 10, 10,
            findPathButton, showConnectionButton, newPlaceButton,
            newConnectionButton, changeConnectionButton);
    buttonsPane.setAlignment(Pos.CENTER);

    VBox layout = new VBox(menuBar, mapView, buttonsPane);
    layout.setSpacing(10);

    Scene scene = new Scene(layout, 700, 800);
    stage.setScene(scene);
    stage.setTitle("PathFinder");
    stage.show();

    //Menyval
    newMapItem.setOnAction(e -> handleNewMap(stage));
    exitItem.setOnAction(e -> stage.close());
  }
  private void disableAllButtons() {
    findPathButton.setDisable(true);
    showConnectionButton.setDisable(true);
    newPlaceButton.setDisable(true);
    newConnectionButton.setDisable(true);
    changeConnectionButton.setDisable(true);
  }
  private void enableAllButtons() {
    findPathButton.setDisable(false);
    showConnectionButton.setDisable(false);
    newPlaceButton.setDisable(false);
    newConnectionButton.setDisable(false);
    changeConnectionButton.setDisable(false);
  }
  private void handleNewMap(Stage stage) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open Map Image");
    fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
    );
    File file = fileChooser.showOpenDialog(stage);
    if (file != null) {
      Image image = new Image(file.toURI().toString());
      mapView.setImage(image);
      mapView.setPreserveRatio(true);
      mapView.setFitWidth(image.getWidth());
      mapView.setFitHeight(image.getHeight());
      enableAllButtons(); // Aktivera knapparna efter bildval
    }
  }


  public static void main(String[] args) {
    launch(args);
  }
}
