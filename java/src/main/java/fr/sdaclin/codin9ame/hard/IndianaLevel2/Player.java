package fr.sdaclin.codin9ame.hard.IndianaLevel2;

import java.util.*;
import java.util.stream.Stream;

class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int W = in.nextInt(); // number of columns.
        int H = in.nextInt(); // number of rows.
        Configuration configuration = new Configuration(W, H);
        in.nextLine();
        for (int i = 0; i < H; i++) {
            String LINE = in.nextLine(); // each line represents a line in the grid and contains W integers T. The absolute value of T specifies the type of the room. If T is negative, the room cannot be rotated.
            configuration.addLine(LINE);
        }
        int EX = in.nextInt(); // the coordinate along the X axis of the exit.
        configuration.setExit(EX);
        in.nextLine();

        // Dump to be able to reproduce in tests
        configuration.dump();

        // Create a solver for this game
        Solver solver = new Solver(configuration);

        // game loop
        while (true) {
            int XI = in.nextInt();
            int YI = in.nextInt();
            String IndyEntrancePoint = in.next();
            solver.setIndyState(XI, YI, IndyEntrancePoint);
            in.nextLine();
            int R = in.nextInt(); // the number of rocks currently in the grid.
            in.nextLine();
            solver.clearRockStates();
            for (int i = 0; i < R; i++) {
                int XR = in.nextInt();
                int YR = in.nextInt();
                String RockEntrancePoint = in.next();
                solver.addRockState(XR, YR, RockEntrancePoint);
                in.nextLine();
            }

            solver.printNextCommand();
        }
    }


    static class Solver {
        private final Configuration configuration;
        private final Tunnel tunnel;
        private State indyState;
        private Set<State> rockStates = new HashSet<>();
        private RoadMap roadMap;

        Solver(Configuration configuration) {
            this.configuration = configuration;
            tunnel = new Tunnel(configuration);
            System.err.println(tunnel);
        }

        public void setIndyState(int xi, int yi, String posi) {
            indyState = new State(xi, yi, Side.forName(posi));
            if (roadMap == null) {
                roadMap = computeNewRoadMap();
            }
        }

        private RoadMap computeNewRoadMap() {
            State currentIndiState = indyState;
            RoadMap roadMap = new RoadMap();
            boolean wayOutIsFound = false;
            do {
                // Brute force :
                // 1 WAIT
                // 2 MOVE LEFT (TIL TWICE if roadMap > 2)
                // 3 MOVE RIGHT ONCE
                Command command = new Command(Command.Verb.WAIT);
                currentIndiState.applyWait(command);
                roadMap.add(command);

            } while (!wayOutIsFound);
            return null;
        }

        public void clearRockStates() {
            rockStates.clear();
        }

        public void addRockState(int xr, int yr, String posr) {
            rockStates.add(new State(xr, yr, Side.forName(posr)));
        }

        /**
         * Print one line containing on of three commands: 'X Y LEFT', 'X Y RIGHT' or 'WAIT'
         */
        public void printNextCommand() {
            Command command = roadMap.peakNext();
            System.out.println(command);
        }

        class State {
            private final int x;
            private final int y;
            private final Side entrancePoint;

            public State(int x, int y, Side entrancePoint) {
                this.x = x;
                this.y = y;
                this.entrancePoint = entrancePoint;
            }

            public State applyWait() {
                Room currentRoom = tunnel.getRoom(this);
                Side outSide = Stream.of(currentRoom.getType().getPaths()).filter(p -> p.from == entrancePoint).findFirst().map(p -> p.to).orElseThrow(MissingEntranceException::new);
                switch (outSide) {
                    case LEFT:

                    case RIGHT:
                    case BOTTOM:
                }
            }

            private class MissingEntranceException extends RuntimeException {
            }
        }

        enum Side {
            TOP, LEFT, RIGHT, BOTTOM;

            public static Side forName(String name) {
                switch (name) {
                    case "TOP":
                        return TOP;
                    case "LEFT":
                        return LEFT;
                    case "RIGHT":
                        return RIGHT;
                    case "BOTTOM":
                        return BOTTOM;
                    default:
                        throw new IllegalArgumentException("Can't find a Side for [" + name + "]");
                }
            }
        }

        enum Path {
            TOP_BOTTOM(Side.TOP, Side.BOTTOM),
            LEFT_BOTTOM(Side.LEFT, Side.BOTTOM),
            RIGHT_BOTTOM(Side.RIGHT, Side.BOTTOM),
            LEFT_RIGHT(Side.LEFT, Side.RIGHT),
            RIGHT_LEFT(Side.RIGHT, Side.LEFT),
            TOP_LEFT(Side.TOP, Side.LEFT),
            TOP_RIGHT(Side.TOP, Side.RIGHT);

            private final Side from;
            private final Side to;

            Path(Side from, Side to) {
                this.from = from;
                this.to = to;
            }
        }

        enum RoomType {
            TYPE_0('.', null),
            TYPE_1('+', Path.TOP_BOTTOM, Path.LEFT_BOTTOM, Path.RIGHT_BOTTOM),
            TYPE_2('↔', Path.LEFT_RIGHT, Path.RIGHT_LEFT),
            TYPE_3('↓', Path.TOP_BOTTOM),
            TYPE_4('⇋', Path.TOP_LEFT, Path.RIGHT_BOTTOM),
            TYPE_5('⇌', Path.TOP_RIGHT, Path.LEFT_BOTTOM),
            TYPE_6('⊥', Path.LEFT_RIGHT, Path.RIGHT_LEFT),
            TYPE_7('⊢', Path.TOP_BOTTOM, Path.RIGHT_BOTTOM),
            TYPE_8('⊤', Path.LEFT_BOTTOM, Path.RIGHT_BOTTOM),
            TYPE_9('⊣', Path.RIGHT_BOTTOM, Path.TOP_BOTTOM),
            TYPE_10('⌊', Path.TOP_LEFT),
            TYPE_11('⌋', Path.TOP_RIGHT),
            TYPE_12('⌈', Path.RIGHT_BOTTOM),
            TYPE_13('⌉', Path.LEFT_BOTTOM);

            private final char display;
            private final Path[] paths;

            RoomType(char display, Path... paths) {
                this.display = display;
                this.paths = paths;
            }

            public static RoomType forIdx(String s) {
                return RoomType.valueOf("TYPE_" + s);
            }

            public String toString() {
                return String.valueOf(display);
            }

            public Path[] getPaths() {
                return paths;
            }
        }

        class Room {
            private final RoomType type;
            private final boolean fixed;

            Room(RoomType type, boolean fixed) {
                this.type = type;
                this.fixed = fixed;
            }

            public RoomType getType() {
                return type;
            }
        }

        private class Tunnel {
            private final int exit;
            private final Room[][] rooms;

            public Tunnel(Configuration configuration) {
                String currentRoomTypeId;
                boolean fixed;
                rooms = new Room[configuration.height][configuration.width];
                for (int heightIdx = 0; heightIdx < configuration.getLines().size(); heightIdx++) {
                    String currentLine = configuration.getLines().get(heightIdx);
                    String[] roomTypeId = currentLine.split("\\s");
                    for (int widthIdx = 0; widthIdx < roomTypeId.length; widthIdx++) {
                        currentRoomTypeId = roomTypeId[widthIdx];
                        fixed = currentRoomTypeId.startsWith("-");
                        if (fixed) {
                            currentRoomTypeId = currentRoomTypeId.substring(1);
                        }
                        RoomType roomType = RoomType.forIdx(currentRoomTypeId);
                        rooms[heightIdx][widthIdx] = new Room(roomType, fixed);
                    }
                }
                exit = configuration.getExit();
            }

            public String toString() {
                StringBuilder builder = new StringBuilder();
                for (int width = 0; width < rooms.length; width++) {
                    for (int height = 0; height < rooms[width].length; height++) {
                        builder.append(rooms[width][height].getType().toString());
                    }
                    builder.append("\n");
                }
                return builder.toString();
            }

            public Room getRoom(State indyState) {
                return rooms[indyState.x][indyState.y];
            }
        }

        private static class Command {
            private final Verb verb;
            private int x;
            private int y;

            public enum Verb {WAIT, LEFT, RIGHT}

            ;

            Command(Verb verb) {
                this.verb = verb;
            }

            Command(Verb verb, int x, int y) {
                if (verb == Verb.WAIT) {
                    throw new IllegalArgumentException("Can't pass coordinate to a WAIT commande");
                }
                this.verb = verb;
                this.x = x;
                this.y = y;
            }
        }

        private class RoadMap {

            List<Command> commands = new ArrayList<>();

            public Command peakNext() {
                return commands.remove(0);
            }

            public void add(Command command) {
                commands.add(command);
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
        private int exit;

        Configuration(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public static Configuration forDump(String dump) {
            String[] lines = dump.split("[\\r\\n]+");
            String[] dimensions = lines[0].split(" ");
            Configuration configuration = new Configuration(Integer.parseInt(dimensions[0]), Integer.parseInt(dimensions[1]));
            for (int i = 1; i < lines.length - 1; i++) {
                String line = lines[i];
                configuration.addLine(line);
            }
            configuration.setExit(Integer.parseInt(lines[lines.length - 1], 10));
            return configuration;
        }

        public void addLine(String line) {
            lines.add(line);
        }

        public void dump() {
            System.err.println(width + " " + height);
            lines.stream().forEach(System.err::println);
            System.err.println(exit);
        }

        public List<String> getLines() {
            return lines;
        }

        public void setExit(int exit) {
            this.exit = exit;
        }

        public int getExit() {
            return exit;
        }
    }
}