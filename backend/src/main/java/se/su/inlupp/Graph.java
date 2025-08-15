// Grupp 114
// Emma Carlsson emka4860
// David Stenberg Kawati dast9610
// Hillevi Sandberg Noppa hisa2469

package se.su.inlupp;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface Graph<T> {

  void add(T node);

  void connect(T node1, T node2, String name, int weight);

  void setConnectionWeight(T node1, T node2, int weight);

  Set<T> getNodes();

  Collection<Edge<T>> getEdgesFrom(T node);

  Edge<T> getEdgeBetween(T node1, T node2);

  void disconnect(T node1, T node2);

  void remove(T node);

  boolean pathExists(T from, T to);

  List<Edge<T>> getPath(T from, T to);
}
