package se.su.inlupp;

import java.util.*;
import se.su.inlupp.Edge;

public class ListGraph<T> implements Graph<T> {

  private Map<T, Set<Edge<T>>> cities = new HashMap<>();

  @Override
  public void add(T node) {
    cities.putIfAbsent(node, new HashSet<>());
  }

  @Override
  public void connect(T node1, T node2, String name, int weight) {
    if (!cities.containsKey(node1) || !cities.containsKey(node2)) {
      throw new NoSuchElementException("En eller båda platserna saknas");
    }

    // Kontrollera om kant redan finns
    if (getEdgeBetween(node1, node2) != null) {
      throw new IllegalStateException("Kanten finns redan");
    }

    // Lägg till två kanter – grafen är oriktad
    cities.get(node1).add(new ListEdge<>(node2, name, weight));
    cities.get(node2).add(new ListEdge<>(node1, name, weight));
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
    if (!cities.containsKey(node)) throw new NoSuchElementException("Platsen saknas");

    return new HashSet<>(cities.get(node));
  }

  @Override
  public Edge<T> getEdgeBetween(T node1, T node2) {
    if (!cities.containsKey(node1) || !cities.containsKey(node2))
      throw new NoSuchElementException("En av platserna saknas");

    for (Edge<T> edge : cities.get(node1)) {
      if (edge.getDestination().equals(node2)) {
        return edge;
      }
    }
    return null;
  }

  @Override
  public void disconnect(T node1, T node2) {
    if (!cities.containsKey(node1) || !cities.containsKey(node2))
      throw new NoSuchElementException("En av platserna saknas");

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

    Set<Edge> edges = new HashSet<>(cities.get(node));

    for (Edge<T> edge : edges) {
      T neighbor = edge.getDestination();
      disconnect(node, neighbor);
    }

    cities.remove(node);
  }

  @Override
  public boolean pathExists(T from, T to) {
    if (!cities.containsKey(from) || !cities.containsKey(to)) {
      return false;
    }

    Set<T> visited = new HashSet<>();
    Queue<T> queue = new LinkedList<>();
    queue.add(from);
    visited.add(from);

    while (!queue.isEmpty()) {
      T current = queue.poll();
      if (current.equals(to)) {
        return true;
      }
      for (Edge<T> edge : cities.get(current)) {
        T neighbor = edge.getDestination();
        if (!visited.contains(edge.getDestination())) {
          visited.add(neighbor);
          queue.add(neighbor);
        }
      }

    }
    return false;
  }

  @Override
    public List<Edge<T>> getPath (T node1, T node2){
      if (!cities.containsKey(node1) || !cities.containsKey(node2)) {
        throw new NoSuchElementException("En eller båda noderna saknas");
      }
      Map<T, T> cameFrom = new HashMap<>();
      cameFrom.put(node1, null);

      Queue<T> queue = new LinkedList<>();
      queue.add(node1);

      while (!queue.isEmpty()) {
        T current = queue.poll();

        for (Edge<T> edge : cities.get(current)) {
          T neighbor = edge.getDestination();
          if (!cameFrom.containsKey(neighbor)) {
            cameFrom.put(neighbor, current);
            queue.add(neighbor);
          }
        }
      }
      if (!cameFrom.containsKey(node2)) {
        return null;
      }
      List<Edge<T>> path = new LinkedList<>();
      T current = node2;

      while (current != null && !current.equals(node1)) {
        T previous = cameFrom.get(current);
        Edge<T> edge = getEdgeBetween(previous, current);
        path.add(0, edge);
        current = previous;
      }
      return path;

    }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    // Lägg till noderna
    for (T node : cities.keySet()) {
      sb.append(node).append("\n");
    }

    // Lägg till alla kanter
    for (T from : cities.keySet()) {
      for (Edge<T> edge : cities.get(from)) {
        sb.append(edge.toString()).append("\n");
      }
    }

    return sb.toString();
  }
}

