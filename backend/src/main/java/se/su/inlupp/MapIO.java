package se.su.inlupp;

import se.su.inlupp.gui.Location;
import java.io.*;
import java.util.*;

public class MapIO {
    public static class LoadedData {
        public String imagePath;
        public List<Location> locations = new ArrayList<>();
        public List<ConnectionData> connections = new ArrayList<>();

    }

    public static class ConnectionData {
        public String from;
        public String to;
        public String name;
        public int weight;

        public ConnectionData(String from, String to, String name, int weight) {
            this.from = from;
            this.to = to;
            this.name = name;
            this.weight = weight;

        }
    }
    public static LoadedData loadGraphFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        LoadedData data = new LoadedData();

        data.imagePath = reader.readLine();

        String nodeLine = reader.readLine();
        if (nodeLine != null && !nodeLine.isEmpty()) {
            String[] nodeParts = nodeLine.split(";");
            for (int i = 0; i < nodeParts.length; i += 3) {
                String name = nodeParts[i];
                double x = Double.parseDouble(nodeParts[i + 1]);
                double y = Double.parseDouble(nodeParts[i + 2]);
                data.locations.add(new Location(name, x, y));
            }
        }
        String line;
        while ((line= reader.readLine()) != null) {
            String[] parts = line.split(";");
            if (parts.length == 4) {
                String from = parts[0];
                String to = parts[1];
                String connectionName = parts [2];
                int weight = Integer.parseInt(parts[3]);
                data.connections.add(new ConnectionData(from, to, connectionName, weight));

            }
        }
        reader.close();
        return data;
    }
}
