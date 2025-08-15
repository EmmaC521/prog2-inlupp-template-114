// Grupp 114
// Emma Carlsson emka4860
// David Stenberg Kawati dast9610
// Hillevi Sandberg Noppa hisa2469

package se.su.inlupp;

public interface Edge<T> {

  int getWeight();

  void setWeight(int weight);

  T getDestination();

  String getName();
}
