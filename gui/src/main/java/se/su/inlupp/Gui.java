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
import javafx.scene.layout.StackPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
  // Lista för att hålla koll på alla platser som läggs till på kartan
  private final List<Location> locations = new ArrayList<>();

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

    //Lägger ihop meny, karta och knappar i en vertikal layout,
    // Ändrat layouten till StackPane så att vi kan lägga platsmarkörer ovanpå kartan
    StackPane mapLayer = new StackPane(mapView);
    VBox layout = new VBox(menuBar, mapLayer, buttonsPane);
    layout.setSpacing(10);

    //Skapar scenen
    Scene scene = new Scene(layout, 700, 850);
    stage.setScene(scene);
    stage.setTitle("PathFinder");
    stage.show();

    //Kopplar menyvalet "New map" till metoden handleNewMap
    newMapItem.setOnAction(e -> handleNewMap(stage));
    //Stänger fönstret när "Exit" väljs
    exitItem.setOnAction(e -> stage.close());
    //  När "New Place" klickas, kör metoden nedan
    newPlaceButton.setOnAction(e -> handleNewPlace());
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
    FileChooser fileChooser = new FileChooser(); //Öppnar filväljaren
    fileChooser.setTitle("Open Map Image"); //Titel i filväljaren
    fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
    );
    File file = fileChooser.showOpenDialog(stage); //Visar dialog och väntar på filval
    if (file != null) {
      //Laddar bilden och den valda filen
      Image image = new Image(file.toURI().toString());
      mapView.setImage(image); //Visar bilden i gränssnittet
      mapView.setPreserveRatio(true); //Bevarar bildens proportioner
      mapView.setFitWidth(600); //Sätter bildens bredd
      mapView.setFitHeight(600); //Sätter bildens höjd
      enableAllButtons(); // Aktivera knapparna efter bildval så att användaren kan fortsätta
    }
  }
  private void handleNewPlace() {
    newPlaceButton.setDisable(true);
    mapView.setCursor(Cursor.CROSSHAIR);

    mapView.setOnMouseClicked(event -> {
      mapView.setCursor(Cursor.DEFAULT);
      newPlaceButton.setDisable(false);
      mapView.setOnMouseClicked(null); // Används bara en gång

      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle("New Place");
      dialog.setHeaderText("Enter name for the new place:");
      Optional<String> result = dialog.showAndWait();

      if (result.isPresent()) {
        String name = result.get().trim();
        if (!name.isEmpty()) {
          double x = event.getX();
          double y = event.getY();

          Location loc = new Location(name, x, y);
          locations.add(loc);

          //  Lägg till platsen ovanpå kartbilden
          ((StackPane) mapView.getParent()).getChildren().add(loc);

          // Gör platsen klickbar för att markera/avmarkera
          loc.setOnMouseClicked(ev -> {
            ev.consume();
            loc.toggleSelection();
          });
        }
      }
    });
  }

  //Startpunkt för programmet
  public static void main(String[] args) {
    launch(args); //Startar JavaFX-applikationen
  }
}

//Klass som representerar en plats på kartan med färg och markeringsstatus
class Location extends Circle {
  private final String name;
  private boolean selected = false;

  public Location(String name, double x, double y) {
    super(x, y, 6); // x, y-position och radie
    this.name = name;
    setFill(Color.BLUE); // Blå = ej vald
    setStroke(Color.BLACK); // Svart kant
    setStrokeWidth(1);
  }

  public String getName() {
    return name;
  }

  public void toggleSelection() {
    selected = !selected;
    setFill(selected ? Color.RED : Color.BLUE); // Röd = vald
  }

  public boolean isSelected() {
    return selected;
  }
}

