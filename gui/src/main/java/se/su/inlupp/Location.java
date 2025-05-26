package se.su.inlupp;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

// Klass som representerar en plats på kartan med färg och markeringsstatus
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

    // ✅ Dessa två metoder SKA ligga inuti klassen
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Location other = (Location) obj;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
