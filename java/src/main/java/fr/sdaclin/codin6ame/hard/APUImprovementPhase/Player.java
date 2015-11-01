package fr.sdaclin.codin6ame.hard.APUImprovementPhase;

import javax.script.ScriptException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * The machines are gaining ground. Time to show them what we're really made of...
 **/
class Player {

    public static void main(String args[]) throws ScriptException {
        PrintStream out = new PrintStream(System.out);
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
    public static class Solver {

        private final List<Node> nodes;

        Solver(Configuration configuration) {
            nodes = readNodes(configuration);
        }

        void solve() {
            List<Node> unconnectedNodes = new ArrayList<>(nodes);


            List<Connexion> stack = new ArrayList<>();
            boolean solutionIsFound = false;
            while (!solutionIsFound) {
                if (context.getUnconnectedNode().size() == 0) {
                    solutionIsFound = true;
                }

                // Try to connect
                try {
                    Context newContext = tryToEstablishAValidConnexion(context);
                } catch (NoValidConnexionExists) {
                    context = stack.remove(stack.size());
                    context = context.nextContextToTry();
                }
            }
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


        private static class Line {
            private Coordinate a;
            private Coordinate b;

            Line(Coordinate a, Coordinate b) {
                this.a = a;
                this.b = b;
            }

            public Coordinate getA() {
                return a;
            }

            public Coordinate getB() {
                return b;
            }
        }

        private static class Coordinate {
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
        }

        private static class Node {
            private final Coordinate coordinate;
            private final int value;

            public Node(Coordinate coordinate, int value) {

                this.coordinate = coordinate;
                this.value = value;
            }

            @Override
            public String toString() {
                return "Node{" +
                        coordinate +
                        ", " + value +
                        '}';
            }

            private class Connexion {

                public Connexion(Node a, Node b, int edgeThickness) {

                }


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
