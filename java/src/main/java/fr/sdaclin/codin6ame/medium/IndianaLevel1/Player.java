package fr.sdaclin.codin6ame.medium.IndianaLevel1;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 */
class Player {


    public static void main(String args[]) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;

        process(inputStream, outputStream);
    }

    static void process(InputStream inputStream, OutputStream outputStream) {
        PrintStream out = new PrintStream(outputStream);
        Scanner in = new Scanner(inputStream);

        int W = in.nextInt(); // number of columns.
        int H = in.nextInt(); // number of rows.
        System.err.println(W);
        System.err.println(H);
        Maze maze = new Maze(W, H);

        in.nextLine();
        for (int i = 0; i < H; i++) {
            String LINE = in.nextLine(); // represents a line in the grid and contains W integers. Each integer represents one room of a given type.
            String[] rooms = LINE.split(" ");
            for (int j = 0; j < rooms.length; j++) {
                maze.setRoom(j, i, new Room(Room.Type.values()[Integer.parseInt(rooms[j], 10)]));
            }
            System.err.println(LINE);
        }

        int EX = in.nextInt(); // the coordinate along the X axis of the exit (not useful for this first mission, but must be read).
        System.err.println(EX);
        maze.setOut(EX);
        in.nextLine();
        System.err.println();

        while (true) {
            int XI = in.nextInt();
            int YI = in.nextInt();
            String POS = in.next();
            in.nextLine();
            System.err.println(XI + " " + YI + " " + POS);

            Coordinate destination = maze.nextRoom(new Coordinate(XI, YI), Room.Link.Side.valueOf(POS));
            out.println(destination.toString());
        }
    }
}

class Maze {
    private final Room[][] rooms;
    private int out;
    private Room[][] room;

    public Maze(int width, int height) {
        this.rooms = new Room[width][];
        for (int i = 0; i < width; i++) {
            this.rooms[i] = new Room[height];
        }
    }

    public void setRoom(int abs, int ord, Room room) {
        rooms[abs][ord] = room;
    }

    public void setOut(int out) {
        //rooms[out][rooms.length-1].setOut(true);
    }

    public Room[][] getRoom() {
        return room;
    }

    public Coordinate nextRoom(Coordinate coordinate, Room.Link.Side side) {
        for (Room.Link link : rooms[coordinate.getAbs()][coordinate.getOrd()].getType().getLinks()) {
            if (link.from != side) {
                continue;
            }
            switch (link.to) {
                case BOTTOM:
                    return new Coordinate(coordinate.getAbs(), coordinate.getOrd() + 1);
                case LEFT:
                    return new Coordinate(coordinate.getAbs() - 1, coordinate.getOrd());
                case RIGHT:
                    return new Coordinate(coordinate.getAbs() + 1, coordinate.getOrd());
            }
        }
        throw new IllegalStateException("WTF");
    }
}

class Room {
    public enum Type {
        TYPE0(),
        TYPE1(new Link(Link.Side.TOP, Link.Side.BOTTOM), new Link(Link.Side.LEFT, Link.Side.BOTTOM), new Link(Link.Side.RIGHT, Link.Side.BOTTOM)),
        TYPE2(new Link(Link.Side.RIGHT, Link.Side.LEFT), new Link(Link.Side.LEFT, Link.Side.RIGHT)),
        TYPE3(new Link(Link.Side.TOP, Link.Side.BOTTOM)),
        TYPE4(new Link(Link.Side.TOP, Link.Side.LEFT), new Link(Link.Side.RIGHT, Link.Side.BOTTOM)),
        TYPE5(new Link(Link.Side.TOP, Link.Side.RIGHT), new Link(Link.Side.LEFT, Link.Side.BOTTOM)),
        TYPE6(new Link(Link.Side.RIGHT, Link.Side.LEFT), new Link(Link.Side.LEFT, Link.Side.RIGHT)),// EQ Type 2
        TYPE7(new Link(Link.Side.TOP, Link.Side.BOTTOM), new Link(Link.Side.RIGHT, Link.Side.BOTTOM)),
        TYPE8(new Link(Link.Side.LEFT, Link.Side.BOTTOM), new Link(Link.Side.RIGHT, Link.Side.BOTTOM)),
        TYPE9(new Link(Link.Side.TOP, Link.Side.BOTTOM), new Link(Link.Side.LEFT, Link.Side.BOTTOM)),
        TYPE10(new Link(Link.Side.TOP, Link.Side.LEFT)),
        TYPE11(new Link(Link.Side.TOP, Link.Side.RIGHT)),
        TYPE12(new Link(Link.Side.RIGHT, Link.Side.BOTTOM)),
        TYPE13(new Link(Link.Side.LEFT, Link.Side.BOTTOM)),;

        private final Link[] links;

        private Type(Link... links) {
            this.links = links;
        }

        public Link[] getLinks() {
            return links;
        }
    }

    public static class Link {
        final Side from;
        final Side to;
        private int absOp;
        private int ordOp;

        public int getAbsOp() {
            switch (to) {
                default:
                case TOP:
                case BOTTOM:
                    return 0;
                case LEFT:
                    return -1;
                case RIGHT:
                    return 1;
            }
        }

        public int getOrdOp() {
            switch (to) {
                default:
                case LEFT:
                case RIGHT:
                    return 0;
                case BOTTOM:
                    return 1;
                case TOP:
                    return -1;
            }
        }

        public enum Side {
            TOP, BOTTOM, LEFT, RIGHT;

            public Side inverse() {
                switch (this) {
                    case TOP:
                        return BOTTOM;
                    case BOTTOM:
                        return TOP;
                    case LEFT:
                        return RIGHT;
                    case RIGHT:
                        return LEFT;
                }
                throw new IllegalStateException();
            }
        }

        public Link(Side from, Side to) {
            this.from = from;
            this.to = to;
        }
    }

    private final Type type;
    private boolean isOut = false;

    public Room(Type type) {
        this.type = type;
    }

    public boolean isOut() {
        return isOut;
    }

    public void setOut(boolean isOut) {
        this.isOut = isOut;
    }

    public Type getType() {
        return type;
    }
}

class Coordinate {

    private int abs;
    private int ord;

    public Coordinate(int abs, int ord) {
        this.abs = abs;
        this.ord = ord;
    }

    @Override
    public String toString() {
        return abs + " " + ord;
    }

    public int getAbs() {
        return abs;
    }

    public int getOrd() {
        return ord;
    }
}
