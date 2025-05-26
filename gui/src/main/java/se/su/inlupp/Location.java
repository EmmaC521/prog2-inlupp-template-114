package se.su.inlupp;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

//Klass som representerar en plats på kartan med färg och markeringsstatus
public class Location extends Circle {
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
