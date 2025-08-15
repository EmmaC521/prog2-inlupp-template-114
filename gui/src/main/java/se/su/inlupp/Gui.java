package se.su.inlupp;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.WritableImage;
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
import javafx.scene.control.ButtonType;
import javafx.scene.shape.Line;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.PrintWriter;

import java.io.File;

public class Gui extends Application {


  //Skapar knapparna
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
  private boolean hasUnsavedChanges = false;

  //Metod som hanterar felmeddelande om det finns osparade ändringar.
  private boolean confirmDiscardUnsavedChanges() {
    if (!hasUnsavedChanges) return true;
    Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Discard unsaved changes?", ButtonType.OK, ButtonType.CANCEL);
    a.setTitle("Unsaved changes");
    return a.showAndWait().filter(bt -> bt == ButtonType.OK).isPresent();
  }

  @Override
  public void start(Stage stage) {

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
    exitItem.setOnAction(e -> { if (confirmDiscardUnsavedChanges()) stage.close(); });
    //openItem.setOnAction(e-> handleOpen(stage)); //Eventhanterare för menyval open
    saveItem.setOnAction(e-> handleSave(stage)); //Eventhanterare för menyval save ev flytta ned

    openItem.setOnAction(e -> handleOpen(stage));
    saveImageItem.setOnAction(e -> handleSaveImage(stage));

    //  När "New Place" klickas, kör metoden nedan
    findPathButton.setOnAction(e -> handleFindPath());
    newPlaceButton.setOnAction(e -> handleNewPlace());
    newConnectionButton.setOnAction(e -> handleNewConnection());
    showConnectionButton.setOnAction(e -> handleShowConnection());
    changeConnectionButton.setOnAction(e -> handleChangeConnection());


    stage.setOnCloseRequest(evt -> { if (!confirmDiscardUnsavedChanges()) evt.consume(); });


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
    //Kontrollerar att det finns en uppladdad karta att spara
    if (mapView.getImage() == null) {
      showError("No map loaded. Cannot save.");
      return;
    }
    //Skapar filväljare och dialog, ser till att filen sparas som *.graph
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save Graph File");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph files", "*.graph"));
    File file = fileChooser.showSaveDialog(stage);
    if (file == null) return;

    //Öppnar filen för att skriva in information
    try (PrintWriter writer = new PrintWriter(file)) {
      //Skriver bildens URL på rad 1
      writer.println(mapView.getImage().getUrl());

      //Skriver alla platser på andra raden
      StringBuilder nodeLine = new StringBuilder();
      for(Location loc : locations) {
        if (!nodeLine.isEmpty())  nodeLine.append(";");
        nodeLine.append(loc.getName()).append(";")
                .append(loc.getX()).append(";")
                .append(loc.getY());
      }
      writer.println(nodeLine);

      //Skriver alla förbindelser på rad 3 och nedåt
      for (Location from : locations) {
        for (Edge<Location> edge : graph.getEdgesFrom(from)) {
          Location to = edge.getDestination();
          if (locations.indexOf(from)<  locations.indexOf(to)) {
            writer.println(from.getName() + ";" + to.getName() + ";" +
                    edge.getName() + ";" + edge.getWeight());
          }
        }
      }
      //Sätts till false då det inte längre finns osparade ändringar
      hasUnsavedChanges = false;
    } catch (IOException e) {
      showError("Could not save" + e.getMessage());
    }

  }

  private void handleNewMap(Stage stage) {
    //Kontrollerar osparade ändringar
    if (!confirmDiscardUnsavedChanges()) return;

    //Skapar filväljare för kartbild och tillåter 4 filformat.
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open Map Image");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

    //Visar dialog
    File file = fileChooser.showOpenDialog(stage);
    if (file == null) return;

    //Rensar nuvarande data och lägger endast till bildvyn/kartan
    locations.clear();
    for (Location n : new ArrayList<>(graph.getNodes())) {
      graph.remove(n);
    }
    mapLayer.getChildren().clear();
    mapLayer.getChildren().add(mapView);

    //Laddar bilden med maxstorlek och behåller proportioner
    Image image = new Image(file.toURI().toString(), 650, 700, true, true);
    mapView.setImage(image);
    mapView.setPreserveRatio(true);
    mapView.setFitWidth(650);
    mapView.setFitHeight(700);

    //Anpassar storlek på mapLayer så den passar bildens storlek
    mapLayer.setPrefWidth(mapView.getBoundsInLocal().getWidth());
    mapLayer.setPrefHeight(mapView.getBoundsInLocal().getHeight());

    //Väntar på att bilden är ritad innan bild centreras och knappar aktiveras
    Platform.runLater(() -> {
      centerImage();
      enableAllButtons();
      hasUnsavedChanges = false;
    });
  }

  //Hjälpmetod för att centrera bild vid uppladnding
  private void centerImage() {
    //Returnerar om ingen bild finns att centrera
    if (mapView.getImage() == null) return;

    double offsetX = (mapLayer.getWidth() - mapView.getBoundsInLocal().getWidth()) / 2;
    double offsetY = (mapLayer.getHeight() - mapView.getBoundsInLocal().getHeight()) / 2;

    mapView.setLayoutX(offsetX);
    mapView.setLayoutY(offsetY);

  }

  //Hanterar först muspekaren när vi ska välja en ny plats (Väntar på klick, stänger av, ber dig skriva in namnet på platsen
  //Skapar platsen, lägger till den i grafen och ritar ut den. Hanterar också ifall man markerar fler än två platser
  private void handleNewPlace() {
    newPlaceButton.setDisable(true);
    mapView.setCursor(Cursor.CROSSHAIR);

    mapView.setOnMouseClicked(event -> {
      mapView.setCursor(Cursor.DEFAULT);
      newPlaceButton.setDisable(false);
      mapView.setOnMouseClicked(null);

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


          ((Pane) mapView.getParent()).getChildren().add(loc);


          loc.setOnMouseClicked(ev -> {
            ev.consume();
            long selectedCount = locations.stream().filter(Location::isSelected).count();

            if (!loc.isSelected() && selectedCount >= 2) {
              showError("You can only select up to TWO places at the same time.");
              return;
            }

            loc.toggleSelection();
          });

          hasUnsavedChanges = true;
        }
      }
    });
  }
  // Hanterar när användaren vill skapa en ny förbindelse mellan två platser
  private void handleNewConnection() {
    // Hämtar alla markerade platser
    List<Location> selected = locations.stream()
            .filter(Location::isSelected)
            .toList();
    // Kontrollera att exakt två platser är markerade
    if (selected.size() != 2) {
      showError("Select exactly TWO places to connect.");
      return;
    }
    // Spara de två valda platserna
    Location from = selected.get(0);
    Location to = selected.get(1);
    // Kolla om förbindelsen redan finns
    if (graph.getEdgeBetween(from, to) != null) {
      showError("Connection already exists between these two places.");
      return;
    }

    // Fråga användaren om typ av förbindelse (t.ex. road, flight)
    TextInputDialog typeDialog = new TextInputDialog("road");
    typeDialog.setTitle("Connection Type");
    typeDialog.setHeaderText("Enter type of connection from " + from.getName() +  " to " + to.getName());
    Optional<String> typeResult = typeDialog.showAndWait();
    if (typeResult.isEmpty()) return;
    String connType = typeResult.get().trim();
    if (connType.isEmpty()) {
      showError("Type cannot be empty.");
      return;
    }

    // Fråga användaren om restid/vikt
    TextInputDialog timeDialog = new TextInputDialog("1");
    timeDialog.setTitle("Connection Time");
    timeDialog.setHeaderText("Enter travel time from " + from.getName() + " to " + to.getName());
    Optional<String> timeResult = timeDialog.showAndWait();
    if (timeResult.isEmpty()) return;
    // Försöker konvertera inmatningen till ett positivt heltal
    try {
      int weight = Integer.parseInt(timeResult.get().trim());
      if (weight <= 0) {
        showError("Time must be a positive integer.");
        return;
      }

      // Lägg till förbindelsen i grafen
      graph.connect(from, to, connType, weight);
      drawConnection(from, to);     // Rita linjen på kartan
      hasUnsavedChanges = true;       // Markera att projektet ändrats sedan senaste sparning

      // Avmarkera platserna efter att förbindelsen skapats
      from.toggleSelection();
      to.toggleSelection();

    } catch (NumberFormatException e) {
      showError("Invalid number. Please enter an integer.");    // Fel om användaren skrev något som inte är ett heltal
    } catch (IllegalStateException e) {
      showError("Connection already exists.");                  // Fel om förbindelsen redan finns
    }
  }

      // Kopplad till knappen "Change Connection" – ändrar vikten (tiden) för en befintlig förbindelse
  private void handleChangeConnection() {
    List<Location> selected = locations.stream()
            .filter(Location::isSelected)
            .toList();
    // Måste vara exakt två markerade för att kunna ändra en förbindelse
    if (selected.size() != 2) {
      showError("Select exactly TWO places to change connection.");
      return;
    }

    Location from = selected.get(0);
    Location to = selected.get(1);
    // Kollar om en förbindelse faktiskt finns
    Edge<Location> edge = graph.getEdgeBetween(from, to);
    if (edge == null) {
      showError("No connection exists between these places.");
      return;
    }


    String currentType = edge.getName();
    String currentTime = String.valueOf(edge.getWeight());

    // Frågar efter det nya värdet
    TextInputDialog timeDialog = new TextInputDialog(currentTime);
    timeDialog.setTitle("Change Connection");
    timeDialog.setHeaderText("Change travel time for: " + from.getName() + " ↔ " + to.getName() +
            "\nType: " + currentType);
    Optional<String> timeResult = timeDialog.showAndWait();
    if (timeResult.isEmpty()) return;

    try {
      int newTime = Integer.parseInt(timeResult.get().trim());
      if (newTime <= 0) {
        showError("Time must be a positive integer.");
        return;
      }


      try {
        edge.setWeight(newTime);
      } catch (NoSuchMethodError | UnsupportedOperationException ex) {

        graph.disconnect(from, to);
        graph.connect(from, to, currentType, newTime);
      }

      hasUnsavedChanges = true;
      from.toggleSelection();
      to.toggleSelection();

    } catch (NumberFormatException e) {
      showError("Invalid number. Please enter an integer.");
    }
  }

  //Använder först hjälpmetoden findPath för att få en lista med platser. Skriver sedan ut listan med
  //hjälp av stringbuilder och en Alert
  private void handleFindPath() {
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

    StringBuilder pathStr = new StringBuilder();
    int totalTime = 0;

    pathStr.append("The Path from ").append(start.getName()).append(" to ").append(end.getName()).append(":\n\n");

    for (int i = 0; i < path.size() - 1; i++) {
      Location from = path.get(i);
      Location to = path.get(i + 1);
      Edge<Location> edge = graph.getEdgeBetween(from, to);

      if (edge != null) {
        pathStr.append("to ")
                .append(to.getName())
                .append(" by ")
                .append(edge.getName())
                .append(" takes ")
                .append(edge.getWeight())
                .append("\n");
        totalTime += edge.getWeight();
      }
    }

    pathStr.append("Total ").append(totalTime);

    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("The Path from " + start.getName() + " to " + end.getName());
    alert.setHeaderText(null);
    alert.setContentText(pathStr.toString());
    alert.showAndWait();

    selectedStart.forEach(Location::toggleSelection);
  }
  // Hämtar alla platser som är markerade just nu
  private void handleShowConnection() {
    List<Location> selected = locations.stream()
            .filter(Location::isSelected)
            .toList();
    // Kontrollerar att exakt två platser är markerade
    if (selected.size() != 2) {
      showError("Select exactly TWO places to check connection.");
      return;
    }
    // Tar fram de två markerade platserna
    Location from = selected.get(0);
    Location to = selected.get(1);

    // Hämtar kanten (förbindelsen) mellan platserna, om den finns
    Edge<Location> edge = graph.getEdgeBetween(from, to);
    if (edge != null) {
      // Om det finns en förbindelse → visa information om den
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Connection Info");
      alert.setHeaderText("There is a connection:");
      alert.setContentText(from.getName() + " ↔ " + to.getName()
              + "\nWeight: " + edge.getWeight()
              + "\nType: " + edge.getName());
      alert.showAndWait();
    } else {
      // Om det inte finns en förbindelse → visa meddelande om det
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("No Connection");
      alert.setHeaderText("No connection exists between:");
      alert.setContentText(from.getName() + " ↔ " + to.getName());
      alert.showAndWait();
    }

    // Avmarkera båda efter visning
    selected.forEach(Location::toggleSelection);
  }

  //En hjälpmetod som används i "handleFindPath" för att hitta en väg mellan två platser med hjälp av
  //bredden-först-sökning. Returnerar sedan en lista med platser från start till slut eller null.
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
        List<Location> path = new LinkedList<>();
        for (Location loc = end; loc != null; loc = cameFrom.get(loc)) {
          path.add(0, loc);
        }
        return path;
      }

      for (Edge<Location> edge : graph.getEdgesFrom(current)) {
        Location neighbor = edge.getDestination();
        if (!cameFrom.containsKey(neighbor)) {
          cameFrom.put(neighbor, current);
          queue.add(neighbor);
        }
      }
    }

    return null;
  }

  
  private void handleOpen(Stage stage) {
    //Kollar om det finns osparade ändringar
    if (!confirmDiscardUnsavedChanges()) return;
    // Öppnar filväljare och dialog och ser till att filen sparas som *.graph
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open Graph File");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph files", "*.graph"));
    File file = fileChooser.showOpenDialog(stage);
    if (file == null) return;

    // Rensar nuvarande data så gammal data inte ligger kvar när vi laddar upp ny bild
    locations.clear();
    mapLayer.getChildren().clear();
    mapLayer.getChildren().add(mapView);

    // Rensar grafen på nuvarande noder
    for (Location location : new ArrayList<>(graph.getNodes())) {
      graph.remove(location);
    }
    //Öppnar och läser från filen
    try (Scanner scanner = new Scanner(file)) {

      // Läser första raden URL till kartbilden
      if (!scanner.hasNextLine()) return;
      String imageUrl = scanner.nextLine().trim();
      //Anpassar kartans storlek och behåller dess proportioner
      Image image = new Image(imageUrl, 650, 700, true, true);
      mapView.setImage(image);
      mapView.setPreserveRatio(true);


      mapLayer.setPrefWidth(650);
      mapLayer.setPrefHeight(700);

      if(!mapLayer.getChildren().contains(mapView)) {
        mapLayer.getChildren().add(mapView);
      }
      //Väntar på att bilden är laddad innan den centreras och knappar aktiveras
      Platform.runLater(() -> {
        centerImage();
        enableAllButtons();
        hasUnsavedChanges = false;
      });

      // Läser andra raden, alla noder och skapar en locationknapp för varje nod
      if (!scanner.hasNextLine()) return;
      String[] nodeParts = scanner.nextLine().split(";");
      for (int i = 0; i < nodeParts.length; i += 3) {
        String name = nodeParts[i].trim();
        double x = Double.parseDouble(nodeParts[i + 1].trim());
        double y = Double.parseDouble(nodeParts[i + 2].trim());

        Location location = new Location(name, x, y);
        locations.add(location);
        graph.add(location);
        mapLayer.getChildren().add(location);

        //Tillåter endast att två platser är markerade samtidigt
        location.setOnMouseClicked(ev -> {
          ev.consume();
          long selectedCount = locations.stream().filter(Location::isSelected).count();

          if (!location.isSelected() && selectedCount >= 2) {
            showError("You can only select up to TWO places at the same time.");
            return;
          }

          location.toggleSelection();
        });
      }

      // Läser in alla förbindelser och ritar linjer på kartan mellan platserna
      while (scanner.hasNextLine()) {
        String[] parts = scanner.nextLine().split(";");
        if (parts.length < 4) continue;

        String fromName = parts[0].trim();
        String toName = parts[1].trim();
        String connName = parts[2].trim();
        int weight = Integer.parseInt(parts[3].trim());

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

    } catch (IOException e) {
      showError("Fel vid inläsning av filen: " + e.getMessage());
      e.printStackTrace();
    }
  }

  // Hanterar menyvalet "Save Image". Sparar ner en skärmdump av kartan och laddar upp den i projektmappen.
  private void handleSaveImage(Stage stage) {
    try{
      WritableImage image = stage.getScene().snapshot(null);
      File file = new File("capture.png");

      ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);


    } catch (IOException ex) {
      showError("Failed to save image: " + ex.getMessage());
    }
  }

  // Hjälpmetod som används för att hitta en plats via namn i en lista.
  private Location findLocationByName(String name) {
    for (Location loc : locations) {
      if (loc.getName().equals(name)) {
        return loc;
      }
    }
    return null;
  }

  // Hämta mittpunkterna i mapLayer-koordinater (parent) för att rita linjen mellan platserna.
  //Ser till så att linjen hamnar i rätt lager (Ovanpå kartan)
  private void drawConnection(Location from, Location to) {

    double startX = from.localToParent(from.getBoundsInLocal()).getMinX() + from.getBoundsInLocal().getWidth() / 2;
    double startY = from.localToParent(from.getBoundsInLocal()).getMinY() + from.getBoundsInLocal().getHeight() / 2;

    double endX = to.localToParent(to.getBoundsInLocal()).getMinX() + to.getBoundsInLocal().getWidth() / 2;
    double endY = to.localToParent(to.getBoundsInLocal()).getMinY() + to.getBoundsInLocal().getHeight() / 2;

    Line line = new Line(startX, startY, endX, endY);
    line.setStroke(Color.BLACK);
    line.setStrokeWidth(2);


    int imgIdx = mapLayer.getChildren().indexOf(mapView);
    mapLayer.getChildren().add(imgIdx + 1, line);
  }

  //Hjälpmetod som andra metoder använder för att skriva utt ERROR-meddelanden. Används för att
  //slippa repetera kod.
  private void showError(String msg) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText(msg);
    alert.showAndWait();
  }

  //Startpunkt för programmet
  public static void main(String[] args) {
    launch(args);
  }
}