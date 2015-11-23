package fr.sdaclin.codin9ame.hard.APUImprovementPhase;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The machines are gaining ground. Time to show them what we're really made of...
 **/
class Player {

    public static void main(String args[]) {
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
        private static final Predicate<Connection> isNotProcessed = (connection -> !connection.isProcessed());
        private final List<Node> nodes = new LinkedList<>();
        private final List<Connection> connections = new LinkedList<>();

        Solver(Configuration configuration) {
            initNodesAndConnections(configuration);
        }

        List<Connection> solve() {
            List<Context> history = new ArrayList<>();
            Context context = null;

            // Auto connect nodes where there are no assumption to be made
            autoSetSureThicknesses(nodes);

            boolean everythingIsProcessed = false;
            do {
                // Get or create current context with a connection to process
                if (context == null) {
                    // Take one random connection that is not processed
                    Optional<Connection> optionalConnection = connections.stream().filter(isNotProcessed).findFirst();
                    if (optionalConnection.isPresent()) {
                        context = new Context(optionalConnection.get(), nodes, connections);
                    } else {
                        everythingIsProcessed = true;
                        continue;
                    }
                }

                // Make an assumption of its potential thickness between 0 to 2
                // Todo add in context to compute only once
                int maxThicknessForThisConnectionNodes = context.getConnection().getA().getWeight() > context.getConnection().getB().getWeight() ? context.getConnection().getB().getWeight() : context.getConnection().getA().getWeight();
                for (int thickness = context.getConnectionThickness(); thickness >= 0; thickness--) {
                    if (thickness > maxThicknessForThisConnectionNodes) {
                        continue;
                    }
                    try {
                        setWeight(context.getConnection(), thickness);
                    } catch (CrossingConnectionException cce) {
                        // Don't need to try other thickness, when this connexion cross another one, the only thickness possible is 0
                        thickness = 1; // Set to 1 and continue in order to try next time directly with 0
                        continue;
                    }
                    context.setConnectionThickness(thickness);
                    break;
                    // Todo Maybe verify if weight > 0 that it remains unprocessed connections on one node ?
                }

                // Save current assumption in history
                history.add(context);
                context = null;
                autoSetSureThicknesses(nodes);
            } while (!everythingIsProcessed);

            return connections;
        }

        /**
         * Cycling each node, find the ones that have a weight that directly match the number of connections and set them
         *      1 nodes with only 1 connexion
         *      nodes with 2 connexions and weight of 4 ( 2 connexions of 2)
         *
         * When all this nodes are auto wired, some connection are bounded to a node that have a weight of 0 so this connection should be flagged as processed
         * @param nodes
         */
        private void autoSetSureThicknesses(List<Node> nodes) {
            final boolean[] oneConnectionHasBeenProcessed = {false};
            do {
                oneConnectionHasBeenProcessed[0] = false;
                nodes.stream()//
                        .filter(node -> !node.isFullyConnected && (node.getWeight() == 1 || node.getWeight() % 2 == 0)) // Matches 1,2,4,6,8
                        .forEach(node -> {
                            Set<Connection> currentNodeUnProcessedConnection = node.getConnections().stream().filter(isNotProcessed).collect(Collectors.toSet());
                            if (currentNodeUnProcessedConnection.size() == 0) {
                                return;
                            }

                            try {
                                if (currentNodeUnProcessedConnection.size() == 1) {
                                    Connection connection = currentNodeUnProcessedConnection.stream().findFirst().get();
                                    assert (node.getWeight() == 1 || node.getWeight() == 2);
                                    setWeight(connection, node.getWeight());
                                    oneConnectionHasBeenProcessed[0] = true;
                                } else if (currentNodeUnProcessedConnection.size() == node.getWeight() / 2) {
                                    assert node.getWeight() != 0;
                                    currentNodeUnProcessedConnection.forEach(connection -> setWeight(connection, 2));
                                    oneConnectionHasBeenProcessed[0] = true;
                                }
                            } catch (CrossingConnectionException e) {
                                e.printStackTrace();
                            }
                        });
            }while(oneConnectionHasBeenProcessed[0]);

            connections.stream()//
                    .filter(connection -> (!connection.isProcessed() && (connection.getA().isFullyConnected || connection.getB().isFullyConnected)))//
                    .forEach(connection1 -> connection1.setProcessed(true));
        }

        private void setWeight(Connection connection, int weight) throws CrossingConnectionException {
            connection.setThickness(weight);
        }

        private Connection findBestConnexionToProcess(Map<Integer, List<Node>> nodesByUnprocessedConnection) {
            return nodesByUnprocessedConnection.values().stream()
                    .filter(list -> list.size() > 0)
                    .limit(1)
                    .map(nodes1 -> nodes1.get(0))
                    .map(node -> node.getUnprocessedConnections().stream().findFirst().get())
                    .findFirst()
                    .get();
        }

        private void initNodesAndConnections(Configuration configuration) {
            Map<Integer, Node> nodeByCol = new HashMap<>();
            for (int i = 0; i < configuration.getLines().size(); i++) {
                String line = configuration.getLines().get(i);
                String[] lineContent = line.split(".{0}");
                Node previousNodeInRow = null;
                for (int j = 0; j < lineContent.length; j++) {
                    String cellContent = lineContent[j];
                    if (cellContent.equals(".")) {
                        continue;
                    }
                    Node node = new Node(new Coordinate(j, i), Integer.parseInt(cellContent, 10));
                    if (previousNodeInRow != null) {
                        Connection connectionTo = node.connect(previousNodeInRow);
                        connections.add(connectionTo);
                    }
                    previousNodeInRow = node;
                    Node previousNodeInCol;
                    if ((previousNodeInCol = nodeByCol.get(j)) != null) {
                        Connection connectFrom = previousNodeInCol.connect(node);
                        connections.add(connectFrom);
                    }
                    nodeByCol.put(j, node);
                    nodes.add(node);
                }
            }
        }

        void printResult() {
            List<Connection> connections = solve();
            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            printOutConnexions(connections, false);
        }

        void printOutConnexions(List<Connection> connections, boolean forTesting) {
            connections.stream().filter(connection -> connection.getThickness() > 0).forEach(connection -> {
                if (forTesting) {
                    System.out.println("System.out.println(\"" + connection.getA().getCoordinate().getX() + " " + connection.getA().getCoordinate().getY() + " " + connection.getB().getCoordinate().getX() + " " + connection.getB().getCoordinate().getY() + " " + connection.getThickness() + "\");");
                } else {
                    System.out.println(connection.getA().getCoordinate().getX() + " " + connection.getA().getCoordinate().getY() + " " + connection.getB().getCoordinate().getX() + " " + connection.getB().getCoordinate().getY() + " " + connection.getThickness());
                }
            });
        }

        static Map<Line, Map<Line, Boolean>> intersectionCache = new HashMap<>();

        static boolean intersects(Line lineA, Line lineB) {
            if (lineA.getOrientation() == lineB.getOrientation()) {
                return false;
            }
            //System.out.println("intersects" + lineA+" "+lineB);
            intersectionCache.computeIfAbsent(lineA, (line -> new HashMap<>()));
            return intersectionCache.get(lineA).computeIfAbsent(lineB, (line -> {
                //System.out.println("from cache" + lineA+" "+lineB);
                int middle;
                int coordinateBound1;
                int coordinateBound2;
                if (lineA.getOrientation() == Line.Orientation.HORIZONTAL) {
                    middle = lineA.getCoordinateOfA().getY();
                    coordinateBound1 = lineB.getCoordinateOfA().getY();
                    coordinateBound2 = lineB.getCoordinateOfB().getY();
                } else {
                    middle = lineA.getCoordinateOfA().getX();
                    coordinateBound1 = lineB.getCoordinateOfA().getX();
                    coordinateBound2 = lineB.getCoordinateOfB().getX();
                }
                return middle != coordinateBound1 && middle != coordinateBound2 && ((middle > coordinateBound1) ? coordinateBound2 > middle : coordinateBound2 < middle);
            }));
        }


        public interface Line {
            enum Orientation {HORIZONTAL, VERTICAL}

            Coordinate getCoordinateOfA();

            Coordinate getCoordinateOfB();

            Orientation getOrientation();
        }

        static class Coordinate {
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
                return x;
            }

            public int getY() {
                return y;
            }
        }

        static class Connection implements Line {
            private final Node nodeA;
            private final Node nodeB;
            private final Orientation orientation;
            private int thickness = 0;
            private boolean processed = false;

            public Connection(Node nodeA, Node nodeB) {
                this.nodeA = nodeA;
                this.nodeB = nodeB;
                this.orientation = (nodeA.getCoordinate().getX() == nodeB.getCoordinate().getX()) ? Orientation.VERTICAL : Orientation.HORIZONTAL;
            }

            public Node getA() {
                return nodeA;
            }

            public Node getB() {
                return nodeB;
            }

            public int getThickness() {
                return thickness;
            }

            public void setThickness(int thickness) {
                processed = true;
                int difference = this.thickness - thickness;
                if (difference != 0) {
                    nodeA.addToWeight(difference);
                    nodeB.addToWeight(difference);
                }
                this.thickness = thickness;
            }

            public void resetThickness() {
                processed = true;
                int difference = this.thickness - thickness;
                if (difference != 0) {
                    nodeA.addToWeight(difference);
                    nodeB.addToWeight(difference);
                }
                this.thickness = 0;
            }

            public int computeMaxThickness() {
                return nodeA.getWeight() < nodeB.getWeight() ? nodeA.getWeight() : nodeB.getWeight();
            }

            @Override
            public Coordinate getCoordinateOfA() {
                return nodeA.coordinate;
            }

            @Override
            public Coordinate getCoordinateOfB() {
                return nodeB.coordinate;
            }

            @Override
            public Orientation getOrientation() {
                return orientation;
            }

            public boolean isProcessed() {
                return processed;
            }

            public void setProcessed(boolean processed) {
                this.processed = processed;
            }
        }

        static class Node {
            private final Coordinate coordinate;
            private int weight;
            private boolean isFullyConnected = false;

            private Set<Connection> connections = new HashSet<>();

            public Node(Coordinate coordinate, int weight) {
                this.coordinate = coordinate;
                this.weight = weight;
            }

            public void addToWeight(int toApply) {
                assert toApply != 0;
                if (this.weight == 0) {
                    assert toApply > 0;
                    isFullyConnected = false;
                }
                this.weight += toApply;
                if (this.weight == 0) {
                    isFullyConnected = true;
                }
            }

            public int getWeight() {
                return weight;
            }

            public Coordinate getCoordinate() {
                return coordinate;
            }

            public Connection connect(Node otherNode) {
                Connection connection = new Connection(this, otherNode);
                connections.add(connection);
                otherNode.connections.add(connection);
                return connection;
            }

            public Set<Connection> getConnections() {
                return connections;
            }

            public Set<Connection> getUnprocessedConnections() {
                return connections.stream().filter((connection) -> !connection.isProcessed()).collect(Collectors.toSet());
            }

            public static boolean hasUnProcessedConnection(Node node) {
                return node.connections.stream().anyMatch(Connection::isProcessed);
            }

            @Override
            public String toString() {
                return "Node{" +
                        "coordinate=" + coordinate +
                        ", weight=" + weight +
                        '}';
            }
        }

        private class Context {
            private Connection connection;
            private final List<Node> nodes;
            private final List<Connection> connections;
            private int connectionThickness;

            public Context(Connection connection, List<Node> nodes, List<Connection> connections) {
                this.connection = connection;
                this.nodes = new LinkedList<>(nodes);
                this.connections = new LinkedList<>(connections);
                this.connectionThickness = 2;
            }

            public Connection getConnection() {
                return connection;
            }

            public int getConnectionThickness() {
                return connectionThickness;
            }

            public void setConnectionThickness(int connectionWeight) {
                this.connectionThickness = connectionWeight;
            }

            public List<Node> getNodes() {
                return nodes;
            }

            public List<Connection> getConnections() {
                return connections;
            }
        }

        private class CrossingConnectionException extends RuntimeException {
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

        public void dump() {
            System.err.println(width + " " + height);
            lines.stream().forEach(System.err::println);

        }

        public List<String> getLines() {
            return lines;
        }
    }
}
