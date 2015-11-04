package fr.sdaclin.codin6ame.hard.APUImprovementPhase;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

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
        private final List<Connexion> connexions = new ArrayList<>();
        private Map<Integer, List<Node>> nodesBySize;
        private List<Node> nodesToConnect;

        Solver(Configuration configuration) {
            nodes = readNodes(configuration);
        }

        List<Connexion> solve() {
            nodesToConnect = new ArrayList<>(nodes);
            nodesBySize = nodes.stream().collect(Collectors.groupingBy(Node::getWeightToppedTo3));


            boolean solutionIsFound = false;
            mainLoop:
            while (!solutionIsFound) {
                // Take the first not fully connected node
                Node currentNode;
                try {
                    currentNode = nodesToConnect.remove(0);
                } catch (IndexOutOfBoundsException iobe) {
                    solutionIsFound = true;
                    continue;
                }
                nodesBySize.get(currentNode.getWeight()).remove(currentNode);
                // Find the max weight of connexion to test
                for (int connexionThickness = currentNode.getWeightToppedTo3(); connexionThickness > 0; connexionThickness--) {
                    // Create a new connexion with the next not connected node
                    Node otherNode;
                    try {
                        otherNode = nodesBySize.get(connexionThickness).remove(0);
                    } catch (IndexOutOfBoundsException iobe) {
                        continue;
                    }
                    Connexion connexion;
                    try {
                        connexion = connect(currentNode, otherNode, connexionThickness);
                    } catch (IntersectionException ie) {
                        if (connexionThickness > 1) {
                            continue;
                        } else {
                            throw new RuntimeException("not implemented yet");
                        }
                    }

                    connexions.add(connexion);

                    try {
                        nodesBySize.get(currentNode.getWeight()).add(currentNode);
                    } catch (NullPointerException npe) {
                        nodesBySize.put(currentNode.getWeight(), new ArrayList<>(singletonList(currentNode)));
                    }
                    try {
                        nodesBySize.get(otherNode.getWeight()).add(otherNode);
                    } catch (NullPointerException npe) {
                        nodesBySize.put(otherNode.getWeight(), new ArrayList<>(singletonList(otherNode)));
                    }
                    if (currentNode.getWeight() != 0) {
                        nodesToConnect.add(0, currentNode);
                    }
                    continue mainLoop;
                }
                // Something goes wrong no connexion can be added

            }
            return connexions;
        }

        private Connexion connect(Node currentNode, Node otherNode, int connexionThickness) throws IntersectionException {
            Connexion connexion = new Connexion(currentNode, otherNode, connexionThickness);
            for (Connexion otherConnexion : connexions) {
                if (intersects(connexion, otherConnexion)) {
                    throw new IntersectionException();
                }
            }
            currentNode.minus(connexionThickness);
            otherNode.minus(connexionThickness);
            return connexion;
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
                    Node node = new Node(new Coordinate(j, i), Integer.parseInt(cellContent, 10));
                    nodes.add(node);
                }
            }
            return nodes;
        }

        void printResult() {
            solve();
            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            System.out.println("0 0 2 0 1"); // Two coordinates and one integer: x node, one of its neighbors, the number of links connecting them.
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

        static class Connexion implements Line {
            private final Node nodeA;
            private final Node nodeB;
            private final int thickness;

            public Connexion(Node nodeA, Node nodeB, int thickness) {
                this.nodeA = nodeA;
                this.nodeB = nodeB;
                this.thickness = thickness;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                Connexion connexion = (Connexion) o;

                if (!nodeA.equals(connexion.nodeA)) return false;
                return nodeB.equals(connexion.nodeB);

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
