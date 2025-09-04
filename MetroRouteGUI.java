import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MetroRouteGUI extends JFrame {

    // ---------------- STATION ----------------
    static class Station {
        String name;
        Set<String> lines = new HashSet<>();
        int lx, ly;  // logical coordinates (0–100)
        Color baseColor;  // idle color (line color)
        Color color;
        List<String> neighbors = new ArrayList<>();

        Station(String name, int lx, int ly, Color baseColor) {
            this.name = name;
            this.lx = lx;
            this.ly = ly;
            this.baseColor = baseColor;
            this.color = baseColor;
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

        void addStation(String name, int lx, int ly, Color c, String line) {
            stations.putIfAbsent(name, new Station(name, lx, ly, c));
            stations.get(name).lines.add(line);
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
        setLayout(new BorderLayout());

        // full screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize);
        graphPanel.setPreferredSize(screenSize);

        JPanel controls = new JPanel();
        controls.add(new JLabel("Source:"));
        controls.add(sourceBox);
        controls.add(new JLabel("Destination:"));
        controls.add(destBox);

        JButton findBtn = new JButton("Dijkstra");
        JButton bfsBtn = new JButton("BFS");
        JButton dfsBtn = new JButton("DFS");

        controls.add(findBtn);
        controls.add(bfsBtn);
        controls.add(dfsBtn);
        add(controls, BorderLayout.NORTH);

        output.setEditable(false);
        add(new JScrollPane(output), BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(graphPanel);
        add(scrollPane, BorderLayout.CENTER);

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

    // ---------------- GRAPH INIT ----------------
    void initGraph() {
        // Colors
        Color BLUE = Color.BLUE;
        Color YELLOW = Color.YELLOW;
        Color RED = Color.RED;

        // ------------ BLUE LINE ------------ (logical coords)
        metroMap.addStation("Rajiv Chowk", 30, 20, BLUE, "Blue");
        metroMap.addStation("Barakhamba", 34, 24, BLUE, "Blue");
        metroMap.addStation("Mandi House", 38, 28, BLUE, "Blue");
        metroMap.addStation("Pragati Maidan", 42, 32, BLUE, "Blue");
        metroMap.addStation("Indraprastha", 46, 36, BLUE, "Blue");
        metroMap.addStation("Yamuna Bank", 50, 40, BLUE, "Blue");
        metroMap.addStation("Akshardham", 54, 38, BLUE, "Blue");
        metroMap.addStation("Mayur Vihar", 58, 36, BLUE, "Blue");
        metroMap.addStation("Mayur Vihar Ext", 62, 34, BLUE, "Blue");
        metroMap.addStation("Noida Sector 15", 66, 32, BLUE, "Blue");
        metroMap.addStation("RK Ashram Marg", 26, 18, BLUE, "Blue");
        metroMap.addStation("Karol Bagh", 22, 16, BLUE, "Blue");
        metroMap.addStation("Rajendra Place", 18, 14, BLUE, "Blue");
        metroMap.addStation("Patel Nagar", 16, 12, BLUE, "Blue");
        metroMap.addStation("Shadipur", 14, 10, BLUE, "Blue");
        metroMap.addStation("Kirti Nagar", 12, 8, BLUE, "Blue");
        metroMap.addStation("Moti Nagar", 10, 6, BLUE, "Blue");
        metroMap.addStation("Ramesh Nagar", 8, 4, BLUE, "Blue");

        // ------------ YELLOW LINE ------------
        metroMap.addStation("Patel Chowk", 26, 15, YELLOW, "Yellow");
        metroMap.addStation("Central Secretariat", 22, 20, YELLOW, "Yellow");
        metroMap.addStation("Udyog Bhawan", 20, 23, YELLOW, "Yellow");
        metroMap.addStation("Race Course", 18, 26, YELLOW, "Yellow");
        metroMap.addStation("Jor Bagh", 16, 29, YELLOW, "Yellow");
        metroMap.addStation("INA", 14, 32, YELLOW, "Yellow");
        metroMap.addStation("AIIMS", 12, 34, YELLOW, "Yellow");
        metroMap.addStation("Green Park", 10, 36, YELLOW, "Yellow");
        metroMap.addStation("Hauz Khas", 8, 38, YELLOW, "Yellow");
        metroMap.addStation("Malviya Nagar", 6, 40, YELLOW, "Yellow");
        metroMap.addStation("New Delhi", 34, 16, YELLOW, "Yellow");
        metroMap.addStation("Chawri Bazar", 37, 14, YELLOW, "Yellow");
        metroMap.addStation("Chandni Chowk", 40, 12, YELLOW, "Yellow");
        metroMap.addStation("Kashmere Gate", 44, 10, YELLOW, "Yellow");
        metroMap.addStation("Civil Lines", 46, 8, YELLOW, "Yellow");
        metroMap.addStation("Vidhan Sabha", 48, 6, YELLOW, "Yellow");
        metroMap.addStation("Vishwavidyalaya", 50, 4, YELLOW, "Yellow");
        metroMap.addStation("GTB Nagar", 52, 2, YELLOW, "Yellow");
        metroMap.addStation("Model Town", 54, 0, YELLOW, "Yellow");

        // ------------ RED LINE ------------
        metroMap.addStation("Pulbangash", 40, 14, RED, "Red");
        metroMap.addStation("Pratap Nagar", 38, 16, RED, "Red");
        metroMap.addStation("Shastri Nagar", 36, 18, RED, "Red");
        metroMap.addStation("Tis Hazari", 42, 12, RED, "Red");
        metroMap.addStation("Shastri Park", 46, 14, RED, "Red");
        metroMap.addStation("Seelampur", 50, 16, RED, "Red");
        metroMap.addStation("Welcome", 54, 18, RED, "Red");
        metroMap.addStation("Shahdara", 58, 20, RED, "Red");
        metroMap.addStation("Mansarovar Park", 62, 22, RED, "Red");
        metroMap.addStation("Jhilmil", 66, 24, RED, "Red");
        metroMap.addStation("Dilshad Garden", 70, 26, RED, "Red");

        // ---------------- EDGES ----------------
        // (same as your original code) ...
        metroMap.addEdge("Rajiv Chowk", "Barakhamba", 1);
        metroMap.addEdge("Barakhamba", "Mandi House", 1);
        metroMap.addEdge("Mandi House", "Pragati Maidan", 2);
        metroMap.addEdge("Pragati Maidan", "Indraprastha", 2);
        metroMap.addEdge("Indraprastha", "Yamuna Bank", 2);
        metroMap.addEdge("Yamuna Bank", "Akshardham", 2);
        metroMap.addEdge("Akshardham", "Mayur Vihar", 2);
        metroMap.addEdge("Mayur Vihar", "Mayur Vihar Ext", 2);
        metroMap.addEdge("Mayur Vihar Ext", "Noida Sector 15", 3);
        metroMap.addEdge("Rajiv Chowk", "RK Ashram Marg", 1);
        metroMap.addEdge("RK Ashram Marg", "Karol Bagh", 2);
        metroMap.addEdge("Karol Bagh", "Rajendra Place", 2);
        metroMap.addEdge("Rajendra Place", "Patel Nagar", 2);
        metroMap.addEdge("Patel Nagar", "Shadipur", 2);
        metroMap.addEdge("Shadipur", "Kirti Nagar", 2);
        metroMap.addEdge("Kirti Nagar", "Moti Nagar", 2);
        metroMap.addEdge("Moti Nagar", "Ramesh Nagar", 2);

        // Yellow
        metroMap.addEdge("Rajiv Chowk", "Patel Chowk", 1);
        metroMap.addEdge("Patel Chowk", "Central Secretariat", 2);
        metroMap.addEdge("Central Secretariat", "Udyog Bhawan", 2);
        metroMap.addEdge("Udyog Bhawan", "Race Course", 2);
        metroMap.addEdge("Race Course", "Jor Bagh", 2);
        metroMap.addEdge("Jor Bagh", "INA", 2);
        metroMap.addEdge("INA", "AIIMS", 2);
        metroMap.addEdge("AIIMS", "Green Park", 2);
        metroMap.addEdge("Green Park", "Hauz Khas", 2);
        metroMap.addEdge("Hauz Khas", "Malviya Nagar", 2);
        metroMap.addEdge("Rajiv Chowk", "New Delhi", 5);
        metroMap.addEdge("New Delhi", "Chawri Bazar", 1);
        metroMap.addEdge("Chawri Bazar", "Chandni Chowk", 1);
        metroMap.addEdge("Chandni Chowk", "Kashmere Gate", 1);
        metroMap.addEdge("Kashmere Gate", "Civil Lines", 2);
        metroMap.addEdge("Civil Lines", "Vidhan Sabha", 2);
        metroMap.addEdge("Vidhan Sabha", "Vishwavidyalaya", 2);
        metroMap.addEdge("Vishwavidyalaya", "GTB Nagar", 2);
        metroMap.addEdge("GTB Nagar", "Model Town", 2);

        // Red
        metroMap.addEdge("Kashmere Gate", "Pulbangash", 2);
        metroMap.addEdge("Pulbangash", "Pratap Nagar", 2);
        metroMap.addEdge("Pratap Nagar", "Shastri Nagar", 2);
        metroMap.addEdge("Kashmere Gate", "Tis Hazari", 2);
        metroMap.addEdge("Tis Hazari", "Shastri Park", 2);
        metroMap.addEdge("Shastri Park", "Seelampur", 2);
        metroMap.addEdge("Seelampur", "Welcome", 2);
        metroMap.addEdge("Welcome", "Shahdara", 2);
        metroMap.addEdge("Shahdara", "Mansarovar Park", 2);
        metroMap.addEdge("Mansarovar Park", "Jhilmil", 2);
        metroMap.addEdge("Jhilmil", "Dilshad Garden", 2);
    }

    // ---------------- DIJKSTRA ----------------
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
            metroMap.stations.get(s).color = metroMap.stations.get(s).baseColor;
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
            for (String at = dst; at != null; at = prev.get(at)) path.add(at);
            Collections.reverse(path);
            output.append("Shortest Path: " + String.join(" → ", path) + "\nTotal Distance: " + dist.get(dst) + " km");
        }).start();
    }

    int getEdgeWeight(String a, String b) {
        for (Edge e : metroMap.edges) {
            if ((e.from.equals(a) && e.to.equals(b)) || (e.from.equals(b) && e.to.equals(a))) return e.distance;
        }
        return 1;
    }

    // ---------------- BFS ----------------
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

    // ---------------- DFS ----------------
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
            if (!visited.contains(v)) dfs(v, visited);
        }
    }

    void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    // ---------------- GRAPH PANEL ----------------
    class GraphPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int w = getWidth();
            int h = getHeight();

            int maxLX = metroMap.stations.values().stream().mapToInt(s -> s.lx).max().orElse(1);
            int maxLY = metroMap.stations.values().stream().mapToInt(s -> s.ly).max().orElse(1);

            // draw edges
            for (Edge e : metroMap.edges) {
                Station a = metroMap.stations.get(e.from);
                Station b = metroMap.stations.get(e.to);

                int ax = a.lx * w / (maxLX + 5);
                int ay = a.ly * h / (maxLY + 5);
                int bx = b.lx * w / (maxLX + 5);
                int by = b.ly * h / (maxLY + 5);

                g.setColor(Color.BLACK);
                g.drawLine(ax, ay, bx, by);
            }

            // draw stations
            for (Station s : metroMap.stations.values()) {
                int sx = s.lx * w / (maxLX + 5);
                int sy = s.ly * h / (maxLY + 5);

                g.setColor(s.color);
                g.fillOval(sx - 10, sy - 10, 20, 20);

                g.setColor(Color.BLACK);
                g.drawString(s.name, sx - 15, sy - 15);
                g.drawOval(sx - 10, sy - 10, 20, 20);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MetroRouteGUI::new);
    }
}
