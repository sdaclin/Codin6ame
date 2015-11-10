package fr.sdaclin.codin9ame.hard.APUImprovementPhase;

import java.util.*;
import java.util.function.Predicate;

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
        private final List<Node> nodes;
        private final List<Connection> connexions = new ArrayList<>();
        private List<Node> unconnectedNodes;

        Solver(Configuration configuration) {
            nodes = readNodes(configuration);
        }

        List<Connection> solve() {
            unconnectedNodes = new ArrayList<>(nodes);
            List<Context> contexts = new ArrayList<>();

            Context context = new Context(0);
            final int[] idx = new int[1];
            idx[0] = 0;
            mainLoop:
            while (unconnectedNodes.size() > 0) {
                // Take current connexion, make an assumption that is ok with bounded nodes, go to next connexion
                if (context.getConnectionIdx() == connexions.size()) {
                    // Wrong path need to revert
                    context = revertAndGetNextContext(contexts);
                }
                final Connection currentConnexion = connexions.get(context.getConnectionIdx());
                // Verify if this connexion doesn't intersect previous connections
                if (currentConnexion.getThickness() > 0 && connexions.stream()
                        .limit(context.getConnectionIdx())
                        .filter(connection -> connection.getThickness() > 0)
                        .reduce(Boolean.FALSE,
                                (result, connexion) -> {
                                    boolean intersects = intersects(connexion, currentConnexion);
                                    return result || intersects;
                                },
                                (res1, res2) -> res1 || res2
                        )) {
                    ArrayList<Connection> allConnections = new ArrayList<>(connexions);
                    allConnections.add(currentConnexion);
                    context = revertAndGetNextContext(contexts);
                }
                for (int thicknessToTry = context.getConnectionThickness(); thicknessToTry >= 0; thicknessToTry--) {
                    if (currentConnexion.computeMaxThickness() < thicknessToTry) {
                        continue;
                    }
                    currentConnexion.setThicknessAndApplyToNode(unconnectedNodes, thicknessToTry);
                    context.setConnectionThickness(thicknessToTry);
                    contexts.add(context);
                    context = new Context(context.getConnectionIdx() + 1);
                    continue mainLoop;
                }
                // Wrong path need to revert
                context = revertAndGetNextContext(contexts);
            }
            return connexions;
        }

        private Context revertAndGetNextContext(List<Context> contexts) {
            Context context;
            do {
                context = contexts.remove(contexts.size() - 1);
                connexions.get(context.getConnectionIdx()).setThicknessAndApplyToNode(unconnectedNodes, context.getConnectionThickness());
            } while (context.getConnectionThickness() == 0);
            // Set context to next (lower) connectionThickness
            context.setConnectionThickness(context.getConnectionThickness() - 1);
            return context;
        }

        private List<Node> readNodes(Configuration configuration) {
            List<Node> nodes = new ArrayList<>();
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
                        connexions.add(connectionTo);
                    }
                    previousNodeInRow = node;
                    Node previousNodeInCol;
                    if ((previousNodeInCol = nodeByCol.get(j)) != null) {
                        Connection connectFrom = previousNodeInCol.connect(node);
                        connexions.add(connectFrom);
                    }
                    nodeByCol.put(j, node);
                    nodes.add(node);
                }
            }
            return nodes;
        }

        void printResult() {
            List<Connection> connections = solve();
            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            printOutConnexions(connections, false);
        }

        private void printOutConnexions(List<Connection> connections, boolean forTesting) {
            connections.stream().filter(connection -> connection.getThickness() > 0).forEach(connection -> {
                if (forTesting) {
                    System.out.println("System.out.println(\"" + connection.getA().getCoordinate().getX() + " " + connection.getA().getCoordinate().getY() + " " + connection.getB().getCoordinate().getX() + " " + connection.getB().getCoordinate().getY() + " " + connection.getThickness() + "\");");
                } else {
                    System.out.println(connection.getA().getCoordinate().getX() + " " + connection.getA().getCoordinate().getY() + " " + connection.getB().getCoordinate().getX() + " " + connection.getB().getCoordinate().getY() + " " + connection.getThickness());
                }
            });
        }

        static Map<Line,Map<Line,Boolean>> intersectionCache = new HashMap<>();
        static boolean intersects(Line lineA, Line lineB) {
            if (lineA.getOrientation() == lineB.getOrientation()) {
                return false;
            }
            //System.out.println("intersects" + lineA+" "+lineB);
            intersectionCache.computeIfAbsent(lineA,(line -> new HashMap<>()));
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

            public void setThicknessAndApplyToNode(List<Node> unconnectedNodes, int thickness) {
                int difference = this.thickness - thickness;
                if (difference != 0) {
                    nodeA.addToWeight(unconnectedNodes, difference);
                    nodeB.addToWeight(unconnectedNodes, difference);
                }
                this.thickness = thickness;
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
        }

        static class Node {
            public void addToWeight(List<Node> unconnectedNodes, int toApply) {
                assert toApply != 0;
                if (this.weight == 0) {
                    assert toApply > 0;
                    unconnectedNodes.add(this);
                }
                this.weight += toApply;
                if (this.weight == 0) {
                    unconnectedNodes.remove(this);
                }
            }

            public enum Axe {X, Y}

            private final Coordinate coordinate;

            private int weight;
            private List<Connection> connections = new ArrayList<>();

            public Node(Coordinate coordinate, int weight) {
                this.coordinate = coordinate;
                this.weight = weight;
            }

            public Connection getConnection(int idx) {
                return this.connections.get(idx);
            }

            public int getWeight() {
                return weight;
            }

            /**
             * Remove connexionThickness to this node weight and return true if this node weight is 0
             *
             * @param connexionThickness
             * @return
             */
            public boolean minus(int connexionThickness) {
                this.weight -= connexionThickness;
                return this.weight == 0;
            }

            public void plus(int connexionThickness) {
                this.weight += connexionThickness;
            }

            public Coordinate getCoordinate() {
                return coordinate;
            }

            public int distance(Node.Axe axe, Node otherNode) {
                switch (axe) {
                    case X:
                        return otherNode.coordinate.getX() - this.coordinate.getX();
                    case Y:
                        return otherNode.coordinate.getY() - this.coordinate.getY();
                    default:
                        throw new RuntimeException("Unsupported exception");
                }
            }

            public Connection connect(Node otherNode) {
                Connection connection = new Connection(this, otherNode);
                connections.add(connection);
                otherNode.connections.add(connection);
                return connection;
            }
        }

        private class IntersectionException extends Exception {
        }

        private class Context {
            private int connectionIdx;
            private int connectionThickness;

            public Context(int connectionIdx) {
                this.connectionIdx = connectionIdx;
                this.connectionThickness = 2;
            }

            public int getConnectionIdx() {
                return connectionIdx;
            }

            public int getConnectionThickness() {
                return connectionThickness;
            }

            public void setConnectionThickness(int connectionWeight) {
                this.connectionThickness = connectionWeight;
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

        public void dump() {
            System.err.println(width + " " + height);
            lines.stream().forEach(System.err::println);

        }

        public List<String> getLines() {
            return lines;
        }
    }
}
