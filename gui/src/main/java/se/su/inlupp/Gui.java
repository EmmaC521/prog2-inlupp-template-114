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

  //Skapar knappar för åtgärder
  private final Button findPathButton = new Button ("Find Path");
  private final Button showConnectionButton = new Button ("Show Connection");
  private final Button newPlaceButton = new Button ("New Place");
  private final Button newConnectionButton = new Button ("New Connection");
  private final Button changeConnectionButton = new Button ("Change Connection");

  //Bildvisare som används för att visa kartan
  private final ImageView mapView = new ImageView();

  @Override
  public void start(Stage stage) {
    //Graph<String> graph = new ListGraph<>(); //Kommer att användas senare

    //Menyn "file"
    Menu fileMenu = new Menu("File");

    //Skapar menyalternativ
    MenuItem newMapItem = new MenuItem("New Map");
    MenuItem openItem = new MenuItem ("Open");
    MenuItem saveItem = new MenuItem("Save");
    MenuItem saveImageItem = new MenuItem("Save Image");
    MenuItem exitItem = new MenuItem("Exit");

    //Lägger till alla menyalternativ i "file"
    fileMenu.getItems().addAll(newMapItem, openItem, saveItem, saveImageItem, exitItem);

    //Skapar menyfältet som visas överst i fönstret
    MenuBar menuBar = new MenuBar(fileMenu);

    //Inaktiverar alla knappar tills att en karta har laddats
    disableAllButtons();

    //Lägger knapparna i en horisontell rad
    FlowPane buttonsPane = new FlowPane(Orientation.HORIZONTAL, 10, 10,
            findPathButton, showConnectionButton, newPlaceButton,
            newConnectionButton, changeConnectionButton);
    buttonsPane.setAlignment(Pos.CENTER);

    //Lägger ihop meny, kartaa och knappar i en vertikal layout
    VBox layout = new VBox(menuBar, mapView, buttonsPane);
    layout.setSpacing(10);

    //Skapar scenen
    Scene scene = new Scene(layout, 700, 800);
    stage.setScene(scene);
    stage.setTitle("PathFinder");
    stage.show();

    //Kopplar menyvalet "New map" till metoden handleNewMap
    newMapItem.setOnAction(e -> handleNewMap(stage));
    //Stänger fönstret när "Exit" väljs"
    exitItem.setOnAction(e -> stage.close());
  }
  //Inaktiverar alla knappar
  private void disableAllButtons() {
    findPathButton.setDisable(true);
    showConnectionButton.setDisable(true);
    newPlaceButton.setDisable(true);
    newConnectionButton.setDisable(true);
    changeConnectionButton.setDisable(true);
  }
  //Aktiverar alla knappar
  private void enableAllButtons() {
    findPathButton.setDisable(false);
    showConnectionButton.setDisable(false);
    newPlaceButton.setDisable(false);
    newConnectionButton.setDisable(false);
    changeConnectionButton.setDisable(false);
  }
  //Metod som körs när användaren väljer "New Map" i menyn
  private void handleNewMap(Stage stage) {
    FileChooser fileChooser = new FileChooser(); //Öppnar filväljare
    fileChooser.setTitle("Open Map Image"); //Titel i vilväljare
    fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
    );
    File file = fileChooser.showOpenDialog(stage); //Visar dialog och väntar på filval
    if (file != null) {
      //Laddar bilden och den valda filen
      Image image = new Image(file.toURI().toString());
      mapView.setImage(image); //Visar bilden i gränssnittet
      mapView.setPreserveRatio(true); //Bevarar bildns proportiner
      mapView.setFitWidth(image.getWidth()); //Sätter bildens bredd
      mapView.setFitHeight(image.getHeight()); //Sätter bildens höjd
      enableAllButtons(); // Aktivera knapparna efter bildval så att användaren kan fortsääta
    }
  }

  //Startpunkt för programmet
  public static void main(String[] args) {
    launch(args); //Startar JavaFX-applikationen
  }
}
