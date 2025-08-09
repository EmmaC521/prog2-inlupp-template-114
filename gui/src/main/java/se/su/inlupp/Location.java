package se.su.inlupp;


import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;


public class Location extends Circle {
    private final String name;
    private final double x, y;
    private boolean selected = false;


    public Location(String name, double x, double y) {
        super(8); // Radie på 8 pixlar
        this.name = name;
        this.x = x;
        this.y = y;


        // Positionera cirkeln på rätt plats
        setCenterX(x);
        setCenterY(y);


        // Sätt färg till blå som standard
        setFill(Color.BLUE);
        setStroke(Color.BLACK);
        setStrokeWidth(1);


        // Gör cirkeln klickbar - viktig del!
        setOnMouseClicked(event -> {
            event.consume(); // Stoppa händelsen från att bubla upp
            toggleSelection();
        });


        // Lägg till hover-effekt för bättre användarupplevelse
        setOnMouseEntered(event -> {
            if (!selected) {
                setFill(Color.LIGHTBLUE);
            }
        });


        setOnMouseExited(event -> {
            if (!selected) {
                setFill(Color.BLUE);
            }
        });
    }


    public void toggleSelection() {
        selected = !selected;
        if (selected) {
            setFill(Color.RED);
        } else {
            setFill(Color.BLUE);
        }
    }


    public boolean isSelected() {
        return selected;
    }


    public String getName() {
        return name;
    }


    public double getX() {
        return x;
    }


    public double getY() {
        return y;
    }


    @Override
    public String toString() {
        return name;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Location location = (Location) obj;
        return name.equals(location.name);
    }


    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
