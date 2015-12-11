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
        //noinspection InfiniteLoopStatement
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
        private final Tunnel tunnel;
        private State indyState;
        private Set<State> rockStates = new HashSet<>();
        private Solution<Command> solution;

        Solver(Configuration configuration) {
            tunnel = new Tunnel(configuration);
            System.err.println(tunnel);
        }

        public void setIndyState(int x, int y, String sideName) {
            indyState = new State(x, y, Side.forName(sideName));
            System.err.println("IndyState: " + indyState);
            if (solution == null) {
                solution = computeNewRoadMap();
            }
        }

        private Solution<Command> computeNewRoadMap() {
            Solution<Command> solution = new Solution<Command>();
            Command currentCommand = null;
            State currentIndiState = indyState;

            boolean wayOutIsFound = false;
            do {
                if (currentCommand == null) {
                    currentCommand = new Command();
                } else {
                    try {
                        currentCommand = nextCommandToTry(currentCommand);
                    } catch (NoMoreCommandToTryException nmctt) {
                        System.out.println("Need to debug this");
                        currentCommand = solution.revert();
                        continue;
                    }
                }

                try {
                    applyCommand(currentCommand);
                    State nextState = autoMove(currentIndiState, tunnel);
                } catch (RuntimeException re) {
                    continue; // to try other command
                }
                solution.add(currentCommand);
            } while (!wayOutIsFound);
            return null;
        }

        private void applyCommand(Command currentCommand) {
            RoomType newType = currentCommand.getRoom().getType().applyRotation(Command.Verb.LEFT);
            currentCommand.getRoom().setType(newType);
        }

        private boolean isValid(State nextState) {
            return false;
        }

        private State autoMove(State currentIndiState, Tunnel tunnel) {
            // In every case indi makes a move
            Room currentIndiRoom = tunnel.getRoom(currentIndiState);
            RoomType type = currentIndiRoom.getType();
            Side outranceSide = outranceSideFor(currentIndiState, type);
            State futureState;
            switch (outranceSide) {
                case BOTTOM:
                    futureState = new State(currentIndiState.x, currentIndiState.y + 1, Side.TOP);
                    break;
                case LEFT:
                    futureState = new State(currentIndiState.x - 1, currentIndiState.y, Side.RIGHT);
                    break;
                case RIGHT:
                    futureState = new State(currentIndiState.x + 1, currentIndiState.y, Side.LEFT);
                    break;
                default:
                    throw new IllegalArgumentException("Can't move outside of a room by its top");
            }
            if (futureState.x < 0 || futureState.x > tunnel.getWidth() || futureState.y > tunnel.getHeight()) {
                throw new CommandMoveIllegal();
            }
            try {
                outranceSideFor(futureState, tunnel.getRoom(futureState).getType());
            } catch (EntranceNotFoundException enfe) {
                throw new CommandMoveIllegal();
            }
            return futureState;
        }

        private Side outranceSideFor(State currentIndiState, RoomType type) {
            return Stream.of(type.getPaths()).filter(path -> path.from == currentIndiState.entrancePoint).map(p -> p.to).findFirst().orElseThrow(EntranceNotFoundException::new);
        }

        private static class Coordinate {
            private final int widthIdx;
            private final int heightIdx;

            public Coordinate(int widthIdx, int heightIdx) {

                this.widthIdx = widthIdx;
                this.heightIdx = heightIdx;
            }

            public int getWidthIdx() {
                return widthIdx;
            }

            public int getHeightIdx() {
                return heightIdx;
            }
        }

        public class NoMoreCommandToTryException extends RuntimeException {
        }

        public class EntranceNotFoundException extends RuntimeException {
        }

        private class CommandMoveIllegal extends RuntimeException {
        }

        /**
         * Compute next initiative after this command
         * Brute force :
         * 1 WAIT
         * 2 Move a non fixed room starting by closest
         * a move left
         * b move right
         *
         * @param currentCommand that gives no result
         * @return next assumption to be made
         */
        private Command nextCommandToTry(Command currentCommand) {
            switch (currentCommand.getVerb()) {
                default:
                case WAIT:
                    return new Command(Command.Verb.LEFT, tunnel.getMovableRooms().get(0));
                case LEFT:
                    return new Command(Command.Verb.RIGHT, currentCommand.getRoom());
                case RIGHT:
                    ArrayList<Room> movableRooms = tunnel.getMovableRooms();
                    int currentRoomIndex = movableRooms.indexOf(currentCommand.getRoom());
                    if (currentRoomIndex + 1 == movableRooms.size()) {
                        throw new NoMoreCommandToTryException();
                    }
                    return new Command(Command.Verb.LEFT, tunnel.getMovableRooms().get(currentRoomIndex + 1));
            }
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
            Command command = solution.peakNext();
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

            @Override
            public String toString() {
                return "State{" +
                        "x=" + x +
                        ", y=" + y +
                        ", entrancePoint=" + entrancePoint +
                        '}';
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

            public RoomType applyRotation(Command.Verb verb) {
                switch (this) {
                    case TYPE_0:
                        return TYPE_0;
                    case TYPE_1:
                        return TYPE_1;
                    case TYPE_2:
                        return TYPE_3;
                    case TYPE_3:
                        return TYPE_2;
                    case TYPE_4:
                        return TYPE_5;
                    case TYPE_5:
                        return TYPE_4;
                    case TYPE_6:
                        return switchForVerb(verb, TYPE_9,TYPE_7);
                    case TYPE_7:
                        return switchForVerb(verb, TYPE_6, TYPE_8);
                    case TYPE_8:
                        return switchForVerb(verb, TYPE_7, TYPE_9);
                    case TYPE_9:
                        return switchForVerb(verb, TYPE_8, TYPE_6);
                    case TYPE_10:
                        return switchForVerb(verb, TYPE_11, TYPE_12);
                    case TYPE_11:
                        return switchForVerb(verb, TYPE_13, TYPE_10);
                    case TYPE_12:
                        return switchForVerb(verb, TYPE_10, TYPE_13);
                    case TYPE_13:
                        return switchForVerb(verb, TYPE_12, TYPE_11);
                    default:
                        throw new IllegalArgumentException();
                }
            }

            private RoomType switchForVerb(Command.Verb verb, RoomType left, RoomType right) {
                switch (verb) {
                    case LEFT:
                        return left;

                    case RIGHT:
                        return right;
                }
                throw new IllegalArgumentException("'Can't switch for other argument");
            }

            public String toString() {
                return String.valueOf(display);
            }

            public Path[] getPaths() {
                return paths;
            }
        }

        class Room {
            private final Coordinate coordinate;
            private final boolean fixed;
            private RoomType type;

            Room(Coordinate coordinate, RoomType type, boolean fixed) {
                this.coordinate = coordinate;
                this.type = type;
                this.fixed = fixed;
            }

            public Coordinate getCoordinate() {
                return coordinate;
            }

            public RoomType getType() {
                return type;
            }

            public void setType(RoomType type) {
                this.type = type;
            }
        }

        private class Tunnel {
            private final int exit;
            private final Room[][] rooms;
            private final int height;
            private final int width;
            private final ArrayList<Room> movableRooms = new ArrayList<>();

            public Tunnel(Configuration configuration) {
                String currentRoomTypeId;
                boolean fixed;
                height = configuration.height;
                width = configuration.width;
                rooms = new Room[height][width];
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
                        Room room = new Room(new Coordinate(widthIdx, heightIdx), roomType, fixed);
                        rooms[heightIdx][widthIdx] = room;
                        if (!fixed) {
                            movableRooms.add(room);
                        }
                    }
                }
                exit = configuration.getExit();
            }

            public ArrayList<Room> getMovableRooms() {
                return movableRooms;
            }

            public Room getRoom(State indyState) {
                return rooms[indyState.y][indyState.x];
            }

            public Room getRoomAt(int x, int y) {
                return rooms[y][x];
            }

            public int getHeight() {
                return height;
            }

            public int getWidth() {
                return width;
            }

            public String toString() {
                StringBuilder builder = new StringBuilder();
                for (int height = 0; height < rooms.length; height++) {
                    for (int width = 0; width < rooms[height].length; width++) {
                        builder.append(rooms[height][width].getType().toString());
                    }
                    builder.append("\n");
                }
                return builder.toString();
            }
        }

        private static class Command {
            private final Verb verb;
            private final Room room;

            public Room getRoom() {
                return room;
            }

            enum Verb {WAIT, LEFT, RIGHT}


            public Verb getVerb() {
                return verb;
            }

            Command() {
                this.room = null;
                this.verb = Verb.WAIT;
            }

            Command(Verb verb, Room room) {
                this.room = room;
                if (verb == Verb.WAIT && room != null) {
                    throw new IllegalArgumentException("Can't pass coordinate to a WAIT commande");
                }

                this.verb = verb;
            }

            public Coordinate getCoordinate() {
                return room.getCoordinate();
            }

            @Override
            public String toString() {
                return "Command{" +
                        "verb=" + verb +
                        ", coordinate=" + room.getCoordinate() +
                        '}';
            }
        }

        private class Solution<STEP> {
            List<STEP> commands = new ArrayList<>();

            public STEP peakNext() {
                return commands.remove(0);
            }

            public void add(STEP step) {
                commands.add(step);
            }

            public STEP revert() {
                return commands.remove(commands.size() - 1);
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