package se.su.inlupp;

public class ListEdge<T> implements Edge<T> {

    private final T destination;
    private final String name;
    private int weight;

    public ListEdge(T destination, String name, int weight) {
        if(weight < 0) throw new IllegalArgumentException("Vikten får inte vara negativ");
        this.destination = destination;
        this.name = name;
        this.weight = weight;
    }

    public T getDestination() {
        return destination;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public void setWeight(int weight) {
        if(weight < 0) throw new IllegalArgumentException("Vikten får inte vara negativ");
        this.weight = weight;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "till " + destination + " med " + name + " tar " + weight;
    }
}