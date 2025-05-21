package se.su.inlupp;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Gui extends Application {

  public void start(Stage stage) {
    Graph<String> graph = new ListGraph<String>();

    GridPane file = new GridPane();
    file.add(new Button("File"), 0, 0);
    file.setAlignment(Pos.TOP_LEFT);

    FlowPane root = new FlowPane();
    root.setOrientation(Orientation.HORIZONTAL);
    root.getChildren().add(new Button("Find Path"));
    root.getChildren().add(new Button("Show Connection"));
    root.getChildren().add(new Button("New Place"));
    root.getChildren().add(new Button("New Connection"));
    root.getChildren().add(new Button("Change Connection"));

    //VBox root = new VBox(30, label);
    root.setAlignment(Pos.TOP_CENTER);

    VBox layout = new VBox(10);
    layout.getChildren().addAll(file, root);

    Scene scene = new Scene(layout, 700, 800);
    stage.setScene(scene);
    stage.setTitle("PathFinder");
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
