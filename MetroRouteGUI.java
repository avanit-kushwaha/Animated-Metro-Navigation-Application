import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MetroRouteGUI extends JFrame {

    static class Station {
        String name, line;
        int x, y;
        Color color = Color.LIGHT_GRAY;
        List<String> neighbors = new ArrayList<>();

        Station(String name, String line, int x, int y) {
            this.name = name;
            this.line = line;
            this.x = x;
            this.y = y;
        }
    }

    static class Edge {
        String from, to;
        int distance;

        Edge(String from, String to, int distance) {
            this.from = from;
            this.to = to;
            this.distance = distance;
        }
    }

    static class Graph {
        Map<String, Station> stations = new HashMap<>();
        List<Edge> edges = new ArrayList<>();

        void addStation(String name, String line, int x, int y) {
            stations.put(name, new Station(name, line, x, y));
        }

        void addEdge(String from, String to, int distance) {
            stations.get(from).neighbors.add(to);
            stations.get(to).neighbors.add(from);
            edges.add(new Edge(from, to, distance));
        }
    }

    Graph metroMap = new Graph();
    JComboBox<String> sourceBox = new JComboBox<>();
    JComboBox<String> destBox = new JComboBox<>();
    JTextArea output = new JTextArea(10, 40);
    GraphPanel graphPanel = new GraphPanel();

    public MetroRouteGUI() {
        setTitle("Delhi Metro Route Finder");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        JPanel controls = new JPanel();
        controls.add(new JLabel("Source:"));
        controls.add(sourceBox);
        controls.add(new JLabel("Destination:"));
        controls.add(destBox);

        JButton findBtn = new JButton("Find Route");
        JButton bfsBtn = new JButton("BFS");
        JButton dfsBtn = new JButton("DFS");

        controls.add(findBtn);
        controls.add(bfsBtn);
        controls.add(dfsBtn);
        add(controls, BorderLayout.NORTH);

        output.setEditable(false);
        add(new JScrollPane(output), BorderLayout.SOUTH);
        add(graphPanel, BorderLayout.CENTER);

        initGraph();

        for (String name : metroMap.stations.keySet()) {
            sourceBox.addItem(name);
            destBox.addItem(name);
        }

        findBtn.addActionListener(e -> animateDijkstra());
        bfsBtn.addActionListener(e -> animateBFS());
        dfsBtn.addActionListener(e -> animateDFS());

        setVisible(true);
    }

    void initGraph() {
        metroMap.addStation("Rajiv Chowk", "Blue", 300, 200);
        metroMap.addStation("Patel Chowk", "Yellow", 250, 150);
        metroMap.addStation("Central Secretariat", "Yellow", 200, 200);
        metroMap.addStation("New Delhi", "Yellow", 350, 150);
        metroMap.addStation("Kashmere Gate", "Red", 400, 100);
        metroMap.addStation("Barakhamba", "Blue", 300, 250);
        metroMap.addStation("Mandi House", "Blue", 300, 300);
        metroMap.addStation("Pragati Maidan", "Blue", 300, 350);
        metroMap.addStation("Indraprastha", "Blue", 300, 400);

        metroMap.addEdge("Rajiv Chowk", "Patel Chowk", 1);
        metroMap.addEdge("Patel Chowk", "Central Secretariat", 2);
        metroMap.addEdge("Central Secretariat", "New Delhi", 3);
        metroMap.addEdge("New Delhi", "Kashmere Gate", 4);
        metroMap.addEdge("Rajiv Chowk", "New Delhi", 5);
        metroMap.addEdge("Rajiv Chowk", "Barakhamba", 1);
        metroMap.addEdge("Barakhamba", "Mandi House", 1);
        metroMap.addEdge("Mandi House", "Pragati Maidan", 2);
        metroMap.addEdge("Pragati Maidan", "Indraprastha", 2);
    }

    void animateDijkstra() {
        String src = (String) sourceBox.getSelectedItem();
        String dst = (String) destBox.getSelectedItem();
        output.setText("");
        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Set<String> visited = new HashSet<>();

        for (String s : metroMap.stations.keySet()) {
            dist.put(s, Integer.MAX_VALUE);
            prev.put(s, null);
            metroMap.stations.get(s).color = Color.LIGHT_GRAY;
        }
        dist.put(src, 0);

        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));
        pq.add(src);

        new Thread(() -> {
            while (!pq.isEmpty()) {
                String u = pq.poll();
                if (visited.contains(u)) continue;
                visited.add(u);
                metroMap.stations.get(u).color = Color.GREEN;
                graphPanel.repaint();
                sleep(700);

                for (String v : metroMap.stations.get(u).neighbors) {
                    int alt = dist.get(u) + getEdgeWeight(u, v);
                    if (alt < dist.get(v)) {
                        dist.put(v, alt);
                        prev.put(v, u);
                        pq.add(v);
                    }
                }
            }

            List<String> path = new ArrayList<>();
            for (String at = dst; at != null; at = prev.get(at)) {
                path.add(at);
            }
            Collections.reverse(path);
            output.append("Shortest Path: " + String.join(" → ", path) + "\nTotal Distance: " + dist.get(dst) + " km");
        }).start();
    }

    int getEdgeWeight(String a, String b) {
        for (Edge e : metroMap.edges) {
            if ((e.from.equals(a) && e.to.equals(b)) || (e.from.equals(b) && e.to.equals(a))) {
                return e.distance;
            }
        }
        return 1;
    }

    void animateBFS() {
        String src = (String) sourceBox.getSelectedItem();
        output.setText("");
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(src);
        visited.add(src);

        new Thread(() -> {
            while (!queue.isEmpty()) {
                String u = queue.poll();
                metroMap.stations.get(u).color = Color.ORANGE;
                graphPanel.repaint();
                output.append(u + " → ");
                sleep(600);

                for (String v : metroMap.stations.get(u).neighbors) {
                    if (!visited.contains(v)) {
                        visited.add(v);
                        queue.add(v);
                    }
                }
            }
        }).start();
    }

    void animateDFS() {
        String src = (String) sourceBox.getSelectedItem();
        output.setText("");
        Set<String> visited = new HashSet<>();
        new Thread(() -> dfs(src, visited)).start();
    }

    void dfs(String u, Set<String> visited) {
        visited.add(u);
        metroMap.stations.get(u).color = Color.CYAN;
        graphPanel.repaint();
        output.append(u + " → ");
        sleep(600);
        for (String v : metroMap.stations.get(u).neighbors) {
            if (!visited.contains(v)) {
                dfs(v, visited);
            }
        }
    }

    void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    class GraphPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            for (Edge e : metroMap.edges) {
                Station a = metroMap.stations.get(e.from);
                Station b = metroMap.stations.get(e.to);
                g.drawLine(a.x, a.y, b.x, b.y);
            }
            for (Station s : metroMap.stations.values()) {
                g.setColor(s.color);
                g.fillOval(s.x - 10, s.y - 10, 20, 20);
                g.setColor(Color.BLACK);
                g.drawString(s.name, s.x - 15, s.y - 15);
                g.drawOval(s.x - 10, s.y - 10, 20, 20);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MetroRouteGUI::new);
    }
}
