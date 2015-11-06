package fr.sdaclin.codin9ame.hard.APUImprovementPhase;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * The machines are gaining ground. Time to show them what we're really made of...
 **/
class Player {

    public static void main(String args[]) throws ScriptException {
        //PrintStream out = new PrintStream(System.out);
        Scanner in = new Scanner(System.in);
        Configuration configuration;

        int width = in.nextInt(); // the number of cells on the X axis
        in.nextLine();
        int height = in.nextInt(); // the number of cells on the Y axis
        in.nextLine();

        configuration = new Configuration(width, height);
        for (int i = 0; i < height; i++) {
            configuration.addLine(in.nextLine()); // width characters, each either x number or x '.'
        }

        // Dump to be able to reproduce in tests
        configuration.dump();

        Solver solver = new Solver(configuration);
        solver.printResult();
    }

    /**
     * The most interesting part
     */
    static class Solver {
        private final List<Node> nodes;
        private final List<Connection> connections = new ArrayList<>();
        private Map<Integer, List<Node>> nodesBySize;
        private List<Node> unconnectedNodes;

        Solver(Configuration configuration) {
            nodes = readNodes(configuration);
        }

        List<Connection> solve() {
            initNodeBySize();
            unconnectedNodes = new ArrayList<>(nodes);
            List<Context> contexts = new ArrayList<>();

            Context context = new Context(peakNextUnconnectedNode());
            mainLoop:
            while (unconnectedNodes.size() > 0) {

                // Try to find a node to connect with
                loopOtherNode:
                for (int i = context.getOtherNodeIndex(); i < unconnectedNodes.size(); i++) {
                    for (int j = context.getConnexionWeight(); j > 0; j--) {
                        Connection connection;
                        try {
                            connection = connect(context.getCurrentNode(), unconnectedNodes.get(context.getOtherNodeIndex()), context.getConnexionWeight());
                            if(unconnectedNodes.size()==0){
                                return connections;
                            }
                        } catch (IntersectionException e) {
                            continue loopOtherNode;
                        }
                        context.setCurrentState(connection,i,j);
                        contexts.add(context);
                        context = new Context(peakNextUnconnectedNode());
                        continue mainLoop;
                    }
                }
                context = contexts.remove(contexts.size()-1);
                disconnect(context.connection);
            }
            return connections;
        }

        private void disconnect(Connection connection) {
            throw new RuntimeException("not implemented yet");
        }

        private void initNodeBySize() {
            nodesBySize = nodes.stream().collect(Collectors.groupingBy(Node::getWeightToppedTo3));
            if (nodesBySize.get(0) == null) {
                nodesBySize.put(0, new ArrayList<>());
            }
            if (nodesBySize.get(1) == null) {
                nodesBySize.put(1, new ArrayList<>());
            }
            if (nodesBySize.get(2) == null) {
                nodesBySize.put(2, new ArrayList<>());
            }
            if (nodesBySize.get(3) == null) {
                nodesBySize.put(3, new ArrayList<>());
            }
        }

        private Node peakNextUnconnectedNode() {
            return unconnectedNodes.remove(0);
        }

        private Connection connect(Node currentNode, Node otherNode, int connexionThickness) throws IntersectionException {
            Connection connection = new Connection(currentNode, otherNode, connexionThickness);
            for (Connection otherConnection : connections) {
                if (    (connection.getA() != otherConnection.getA()
                        && connection.getA() != otherConnection.getB()
                        && connection.getB() != otherConnection.getA()
                        && connection.getB() != otherConnection.getB())
                        && intersects(connection, otherConnection)) {
                    throw new IntersectionException();
                }
            }
            currentNode.minus(connexionThickness);
            if(currentNode.weight>0){
                unconnectedNodes.add(currentNode);
            }
            otherNode.minus(connexionThickness);
            if(otherNode.weight==0){
                unconnectedNodes.remove(otherNode);
            }
            connections.add(connection);
            return connection;
        }

        private List<Node> readNodes(Configuration configuration) {
            List<Node> nodes = new ArrayList<>();
            for (int i = 0; i < configuration.getLines().size(); i++) {
                String line = configuration.getLines().get(i);
                String[] lineContent = line.split(".{0}");
                for (int j = 0; j < lineContent.length; j++) {
                    String cellContent = lineContent[j];
                    if (cellContent.equals(".")) {
                        continue;
                    }
                    Node node = new Node(new Coordinate(i, j), Integer.parseInt(cellContent, 10));
                    nodes.add(node);
                }
            }
            return nodes;
        }

        void printResult() {
            List<Connection> connections = solve();
            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            connections.stream().forEach(connection -> {
                System.out.println(connection.getA().getX() + " " + connection.getA().getY() + " " + connection.getB().getX() + " " + connection.getB().getY() + " " + connection.getThickness());
            });
            //System.out.println("0 0 2 0 1"); // Two coordinates and one integer: x node, one of its neighbors, the number of links connecting them.
        }

        boolean intersects(Line lineA, Line lineB) {
            int p0_y = lineA.getA().getY();
            int p0_x = lineA.getA().getX();
            int p1_x = lineA.getB().getX();
            int p1_y = lineA.getB().getY();
            int p2_x = lineB.getA().getX();
            int p2_y = lineB.getA().getY();
            int p3_x = lineB.getB().getX();
            int p3_y = lineB.getB().getY();

            int s1_x, s1_y, s2_x, s2_y;
            s1_x = p1_x - p0_x;
            s1_y = p1_y - p0_y;
            s2_x = p3_x - p2_x;
            s2_y = p3_y - p2_y;

            int s, t;
            s = (-s1_y * (p0_x - p2_x) + s1_x * (p0_y - p2_y)) / (-s2_x * s1_y + s1_x * s2_y);
            t = (s2_x * (p0_y - p2_y) - s2_y * (p0_x - p2_x)) / (-s2_x * s1_y + s1_x * s2_y);

            return s >= 0 && s <= 1 && t >= 0 && t <= 1;
        }


        private interface Line {
            Coordinate getA();

            Coordinate getB();
        }

        private static class Coordinate implements Comparable<Coordinate> {
            private final int x;
            private final int y;

            Coordinate(int x, int y) {
                this.x = x;
                this.y = y;
            }

            @Override
            public String toString() {
                return "{" +
                        "" + x +
                        ", " + y +
                        '}';
            }

            public int getX() {
                return y;
            }

            public int getY() {
                return x;
            }

            @Override
            public int compareTo(Coordinate o) {
                if (x < o.x) {
                    return -1;
                } else if (x > o.x) {
                    return +1;
                } else {
                    if (y < o.y) {
                        return -1;
                    }
                    if (y > o.y) {
                        return +1;
                    }
                    return 0;
                }
            }
        }

        static class Connection implements Line {
            private final Node nodeA;
            private final Node nodeB;
            private final int thickness;

            public Connection(Node nodeA, Node nodeB, int thickness) {
                this.nodeA = nodeA;
                this.nodeB = nodeB;
                this.thickness = thickness;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                Connection connection = (Connection) o;

                if (!nodeA.equals(connection.nodeA)) return false;
                return nodeB.equals(connection.nodeB);

            }

            @Override
            public int hashCode() {
                int result = nodeA.hashCode();
                result = 31 * result + nodeB.hashCode();
                return result;
            }

            @Override
            public Coordinate getA() {
                return nodeA.coordinate;
            }

            @Override
            public Coordinate getB() {
                return nodeB.coordinate;
            }

            public int getThickness() {
                return thickness;
            }
        }

        static class Node {
            private final Coordinate coordinate;
            private int weight;

            public Node(Coordinate coordinate, int weight) {
                this.coordinate = coordinate;
                this.weight = weight;
            }

            public int getWeight() {
                return weight;
            }

            public int getWeightToppedTo3() {
                return weight > 3 ? 3 : weight;
            }

            public void minus(int connexionThickness) {
                this.weight -= connexionThickness;
            }

            public void plus(int connexionThickness) {
                this.weight += connexionThickness;
            }
        }

        private class IntersectionException extends Exception {
        }

        private class Context {
            private final Node currentNode;
            private int connexionWeight;
            private Connection connection;
            private int otherNodeIndex;

            public Context(Node currentNode) {
                this(currentNode, 0, currentNode.getWeightToppedTo3());
            }

            private Context(Node currentNode, int otherNodeIndex, int currentWeight) {
                this.currentNode = currentNode;
                this.otherNodeIndex = otherNodeIndex;
                this.connexionWeight = currentWeight;
            }

            public Node getCurrentNode() {
                return currentNode;
            }

            public int getConnexionWeight() {
                return connexionWeight;
            }

            public int getOtherNodeIndex() {
                return otherNodeIndex;
            }

            public void setCurrentState(Connection connection, int otherNodeIndex, int connexionWeight) {
                this.connection = connection;
                this.otherNodeIndex = otherNodeIndex;
                this.connexionWeight = connexionWeight;
            }
        }
    }

    /**
     * A lightweight structure to be able to dump and restore level configuration
     */
    static class Configuration {
        private final int width;
        private final int height;
        private final List<String> lines = new ArrayList<>();

        Configuration(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public static Configuration forDump(String dump) {
            String[] lines = dump.split("[\\r\\n]+");
            String[] dimensions = lines[0].split(" ");
            Configuration configuration = new Configuration(Integer.parseInt(dimensions[0]), Integer.parseInt(dimensions[1]));
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                configuration.addLine(line);
            }
            return configuration;
        }

        public void addLine(String line) {
            lines.add(line);
        }

        public void dump() throws ScriptException {
            System.err.println(width + " " + height);
            lines.stream().forEach(System.err::println);

        }

        public List<String> getLines() {
            return lines;
        }
    }
}
