package se.su.inlupp;

import java.util.*;
import se.su.inlupp.Edge;

public class ListGraph<T> implements Graph<T> {

  private Map<T, Set<Edge>> cities = new HashMap<>();

  @Override
  public void add(T node) {
    cities.putIfAbsent(node, new HashSet<>());
  }

  @Override
  public void connect(T node1, T node2, String name, int weight) {
    add(node1);
    add(node2);

    Set<Edge> fromCities = cities.get(node1);
    Set<Edge> toCities = cities.get(node2);

    fromCities.add(new ListEdge(node1, name, weight));
    toCities.add(new ListEdge(node2, name, weight));
  }

  @Override
  public void setConnectionWeight(T node1, T node2, int weight) {
    if (weight < 0) throw new IllegalArgumentException("Vikten får inte vara negativ");

    if (!cities.containsKey(node1) || !cities.containsKey(node2)) {
      throw new NoSuchElementException("En eller båda platserna saknas");
    }

    Edge<T> edge1 = getEdgeBetween(node1, node2);
    Edge<T> edge2 = getEdgeBetween(node2, node1);

    if (edge1 == null || edge2 == null) {
      throw new NoSuchElementException("Platserna har ingen förbindelse");
    }

    edge1.setWeight(weight);
    edge2.setWeight(weight);
  }

  @Override
  public Set<T> getNodes() {
    return new HashSet<>(cities.keySet());
  }

  @Override
  public Collection<Edge<T>> getEdgesFrom(T node) {
    if (!cities.containsKey(node)) throw new IllegalArgumentException("Platsen saknas");

    return new HashSet<>(cities.get(node));
  }

  @Override
  public Edge<T> getEdgeBetween(T node1, T node2) {
    if (!cities.containsKey(node1) || !cities.containsKey(node2)) throw new IllegalArgumentException("En av platserna saknas");

    for (Edge<T> edge : cities.get(node1)) {
      if (edge.getDestination().equals(node2)) {
        return edge;
      }
    }
    return null;
  }

  @Override
  public void disconnect(T node1, T node2) {
    if (!cities.containsKey(node1) || !cities.containsKey(node2)) throw new IllegalArgumentException("En av platserna saknas");

    Edge<T> edge1 = getEdgeBetween(node1, node2);
    Edge<T> edge2 = getEdgeBetween(node2, node1);

    if (edge1 == null || edge2 == null) {
      throw new IllegalStateException("Det saknas förbindelse mellan dessa två platser");
    }

    cities.get(node1).remove(edge1);
    cities.get(node2).remove(edge2);
  }

  @Override
  public void remove(T node) {
    if (!cities.containsKey(node)) throw new NoSuchElementException("Platsen saknas");

    for (Edge<T> edge : new HashSet<>(cities.get(node)) {
       bla bla bla bla;

    }

  }

  @Override
  public boolean pathExists(T from, T to) {
    throw new UnsupportedOperationException("Unimplemented method 'pathExists'");
  }

  @Override
  public List<Edge<T>> getPath(T from, T to) {
    throw new UnsupportedOperationException("Unimplemented method 'getPath'");
  }
}