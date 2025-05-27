package se.su.inlupp;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.TextInputDialog;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.application.Platform;

import java.util.*;

import javafx.scene.control.Alert;
import javafx.scene.shape.Line;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

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
  private final Graph<Location> graph = new ListGraph<>();
  private final Pane mapLayer = new Pane();

  @Override
  public void start(Stage stage) {
    //Kommer att användas senare

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
    mapLayer.getChildren().add(mapView);
    VBox topBox = new VBox(menuBar, buttonsPane);
    topBox.setAlignment(Pos.CENTER);
    topBox.setSpacing(10);

    BorderPane root = new BorderPane();
    root.setTop(topBox);
    root.setCenter(mapLayer); // ← mapLayer (med bild) hamnar i mitten

    Scene scene = new Scene(root, 700, 850);
    stage.setScene(scene);
    stage.setTitle("PathFinder");
    stage.show();

    //Kopplar menyvalet "New map" till metoden handleNewMap
    newMapItem.setOnAction(e -> handleNewMap(stage));
    //Stänger fönstret när "Exit" väljs
    exitItem.setOnAction(e -> stage.close());
    //openItem.setOnAction(e-> handleOpen(stage)); //Eventhanterare för menyval open
    saveItem.setOnAction(e-> handleSave(stage)); //Eventhanterare för menyval save ev flytta ned

    openItem.setOnAction(e -> handleOpen(stage));

    //  När "New Place" klickas, kör metoden nedan
    findPathButton.setOnAction(e -> handleFindPath());
    newPlaceButton.setOnAction(e -> handleNewPlace());
    newConnectionButton.setOnAction(e -> handleNewConnection());
    showConnectionButton.setOnAction(e -> handleShowConnection());


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

  //Metod för menyvalet "Save"
  private void handleSave(Stage stage) {
    if (mapView.getImage() == null) {
      showError("No map loaded. Cannot save.");
      return;
    }
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save Graph File");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph files", "*.graph"));
    File file = fileChooser.showSaveDialog(stage);
    if (file == null) return;

    try (PrintWriter writer = new PrintWriter(file)) {
      writer.println(mapView.getImage().getUrl());

      StringBuilder nodeLine = new StringBuilder();
      for(Location loc : locations) {
        if (!nodeLine.isEmpty())  nodeLine.append(";");
        nodeLine.append(loc.getName()).append(";")
                .append(loc.getCenterX()).append(";")
                .append(loc.getCenterY());
      }
      writer.println(nodeLine);


      for (Location from : locations) {
        for (Edge<Location> edge : graph.getEdgesFrom(from)) {
          Location to = edge.getDestination();
          if (locations.indexOf(from)<  locations.indexOf(to)) {
            writer.println(from.getName() + ";" + to.getName() + ";" +
                    edge.getName() + ";" + edge.getWeight());
          }
        }
      }
      //hasUnsavedChanges = false; //Kommer att använas senare
    } catch (IOException e) {
      showError("Could not save" + e.getMessage());
    }

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
      mapView.setFitWidth(650); //Sätter bildens bredd
      mapView.setFitHeight(700); //Sätter bildens höjd

      mapLayer.setPrefWidth(mapView.getFitWidth());
      mapLayer.setPrefHeight(mapView.getFitHeight());

      if (!mapLayer.getChildren().contains(mapView)) {
        mapLayer.getChildren().add(mapView);
      }

      // Vänta på att bilden är helt laddad innan centrering
      Platform.runLater(() -> {
        mapView.setLayoutX((mapLayer.getWidth() - mapView.getBoundsInLocal().getWidth()) / 2);
        mapView.setLayoutY((mapLayer.getHeight() - mapView.getBoundsInLocal().getHeight()) / 2);
      });

      enableAllButtons();// Aktivera knapparna efter bildval så att användaren kan fortsätta
    }
  }
  private void centerImage() {
    mapView.setLayoutX((mapLayer.getWidth() - mapView.getBoundsInLocal().getWidth()) / 2);
    mapView.setLayoutY((mapLayer.getHeight() - mapView.getBoundsInLocal().getHeight()) / 2);
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
          double x = event.getX() + mapView.getLayoutX();
          double y = event.getY() + mapView.getLayoutY();

          Location loc = new Location(name, x, y);
          locations.add(loc);
          graph.add(loc);

          //  Lägg till platsen ovanpå kartbilden
          ((Pane) mapView.getParent()).getChildren().add(loc);

          // Gör platsen klickbar för att markera/avmarkera
          loc.setOnMouseClicked(ev -> {
            ev.consume();
            loc.toggleSelection();
          });
        }
      }
    });
  }

  private void handleNewConnection() {
    List<Location> selected = locations.stream()
            .filter(Location::isSelected)
            .toList();

    if (selected.size() != 2) {
      showError("Select exactly TWO places to connect.");
      return;
    }

    Location from = selected.get(0);
    Location to = selected.get(1);

    TextInputDialog dialog = new TextInputDialog("1");
    dialog.setTitle("Connection Weight");
    dialog.setHeaderText("Enter weight for the connection:");
    Optional<String> result = dialog.showAndWait();

    if (result.isPresent()) {
      try {
        int weight = Integer.parseInt(result.get().trim());

        graph.connect(from, to, "road", weight);

        Line line = new Line(from.getCenterX(), from.getCenterY(),
                to.getCenterX(), to.getCenterY());
        line.setStroke(Color.GRAY);
        ((StackPane) mapView.getParent()).getChildren().add(0, line);

        from.toggleSelection();
        to.toggleSelection();

      } catch (NumberFormatException e) {
        showError("Invalid number. Please enter an integer.");
      }
    }
  }

  private void handleShowConnection() {
    List<Location> selected = locations.stream()
            .filter(Location::isSelected)
            .toList();

    if (selected.size() != 2) {
      showError("Select exactly TWO places to check connection.");
      return;
    }

    Location from = selected.get(0);
    Location to = selected.get(1);

    if (graph.pathExists(from, to)) {
      Edge<Location> edge = graph.getEdgeBetween(from, to);

      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Connection Info");
      alert.setHeaderText("There is a connection:");
      alert.setContentText(from.getName() + " ↔ " + to.getName()
              + "\nWeight: " + edge.getWeight()
              + "\nType: " + edge.getName());
      alert.showAndWait();
    } else {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("No Connection");
      alert.setHeaderText("No connection exists between:");
      alert.setContentText(from.getName() + " ↔ " + to.getName());
      alert.showAndWait();
    }

    // Avmarkera båda efter visning
    selected.forEach(Location::toggleSelection);
  }

  private void handleFindPath() {
    // Välj startplats
    List<Location> selectedStart = locations.stream()
            .filter(Location::isSelected)
            .toList();

    if (selectedStart.size() != 2) {
      showError("Select exactly TWO places to find path.");
      return;
    }

    Location start = selectedStart.get(0);
    Location end = selectedStart.get(1);

    List<Location> path = findPath(start, end);

    if (path == null || path.isEmpty()) {
      showError("No path found between selected locations.");
      return;
    }

    // Markera eller visa vägen på något sätt
    StringBuilder pathStr = new StringBuilder();
    for (Location loc : path) {
      if (pathStr.length() > 0) pathStr.append(" -> ");
      pathStr.append(loc.getName());
    }

    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Found Path");
    alert.setHeaderText("Path from " + start.getName() + " to " + end.getName());
    alert.setContentText(pathStr.toString());
    alert.showAndWait();

    // Avmarkera platser efteråt
    selectedStart.forEach(Location::toggleSelection);
  }

  private List<Location> findPath(Location start, Location end) {
    if (start.equals(end)) {
      return List.of(start);
    }

    Queue<Location> queue = new LinkedList<>();
    Map<Location, Location> cameFrom = new HashMap<>();

    queue.add(start);
    cameFrom.put(start, null);

    while (!queue.isEmpty()) {
      Location current = queue.poll();

      if (current.equals(end)) {
        // Bygg vägen baklänges
        List<Location> path = new LinkedList<>();
        for (Location loc = end; loc != null; loc = cameFrom.get(loc)) {
          path.add(0, loc);
        }
        return path;
      }

      // Utforska grannarna
      for (Edge<Location> edge : graph.getEdgesFrom(current)) {
        Location neighbor = edge.getDestination();
        if (!cameFrom.containsKey(neighbor)) {
          cameFrom.put(neighbor, current);
          queue.add(neighbor);
        }
      }
    }

    // Ingen väg hittades
    return null;
  }
  private void handleOpen(Stage stage) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open Graph File");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph files", "*.graph"));
    File file = fileChooser.showOpenDialog(stage);
    if (file == null) return;

    locations.clear();
    mapLayer.getChildren().clear();
    mapLayer.getChildren().add(mapView);

    for (Location location : new ArrayList<>(graph.getNodes())) {
      graph.remove(location);
    }

    try (Scanner scanner = new Scanner(file)) {
      if(!scanner.hasNextLine()) return;
      String[] placeData = scanner.nextLine().split(";");
      String imageFileName = placeData[0].replace("file:", "").trim();
      File imageFile = new File(file.getParentFile(), imageFileName);
      Image image = new Image(imageFile.toURI().toString());
      mapView.setImage(image);
      mapView.setPreserveRatio(true);
      mapView.setMouseTransparent(true);
      mapView.setFitWidth(650);
      mapView.setFitHeight(700);

      mapLayer.setPrefWidth(mapView.getFitWidth());
      mapLayer.setPrefHeight(mapView.getFitHeight());

      Platform.runLater(() -> {
        mapView.setLayoutX((mapLayer.getWidth() - mapView.getBoundsInLocal().getWidth()) / 2);
        mapView.setLayoutY((mapLayer.getHeight() - mapView.getBoundsInLocal().getHeight()) / 2);

        enableAllButtons();
      });

      for (int i = 1; i < placeData.length; i += 3) {
        String name = placeData[i].trim();
        double x = Double.parseDouble(placeData[i + 1].trim());
        double y = Double.parseDouble(placeData[i + 2].trim());
        Location location = new Location(name, 0, 0); // temporär
        location.setLayoutX(x);
        location.setLayoutY(y);

        location.setOnMouseClicked(ev -> {
          ev.consume();
          location.toggleSelection();
        });

        locations.add(location);
        graph.add(location);
        mapLayer.getChildren().add(location);
      }

      List<String> edgeParts = new ArrayList<>();
      while (scanner.hasNextLine()) {
        String[] parts = scanner.nextLine().split(";");
        for (String part : parts) {
          edgeParts.add(part.trim());
        }
      }

      for (int i = 0; i < edgeParts.size(); i += 4) {
        String fromName = edgeParts.get(i);
        String toName = edgeParts.get(i + 1);
        String connName = edgeParts.get(i + 2);
        int weight = Integer.parseInt(edgeParts.get(i + 3));

        Location from = findLocationByName(fromName);
        Location to = findLocationByName(toName);
        if (from != null && to != null) {
          try {
            graph.connect(from, to, connName, weight);
            drawConnection(from, to);
          } catch (Exception e) {
            System.err.println("Kunde inte koppla " + fromName + " - " + toName + ": " + e.getMessage());
          }
        }
      }
    } catch (FileNotFoundException e) {
      showErrorDialog("Fel vid inläsning av filen: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private Location findLocationByName(String name) {
    for (Location loc : locations) {
      if (loc.getName().equals(name)) return loc;
    }
    return null;
  }

  private void drawConnection(Location from, Location to) {
    Line line = new Line();
    line.setStartX(from.getTranslateX());
    line.setStartY(from.getTranslateY());
    line.setEndX(to.getTranslateX());
    line.setEndY(to.getTranslateY());
    line.setStroke(Color.BLACK);
    line.setStrokeWidth(2);
    mapLayer.getChildren().add(line);
  }

  private void showErrorDialog(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Fel");
    alert.setHeaderText("Ett fel inträffade");
    alert.setContentText(message);
    alert.showAndWait();
  }



  private void showError(String msg) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText(msg);
    alert.showAndWait();
  }




  //Startpunkt för programmet
  public static void main(String[] args) {
    launch(args); //Startar JavaFX-applikationen
  }
}


