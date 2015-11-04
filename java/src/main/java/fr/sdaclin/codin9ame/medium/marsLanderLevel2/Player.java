package fr.sdaclin.codin9ame.medium.marsLanderLevel2;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {
    private static final int AXE_X = 0;
    private static final int AXE_Y = 1;
    private static final int EST = 0;
    private static final int WEST = 1;
    private static final float GRAVITY = 3.711f;

    public static void main(String args[]) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;

        process(inputStream, outputStream);
    }

    static void process(InputStream inputStream, OutputStream outputStream) {
        PrintStream out = new PrintStream(outputStream);
        Scanner in = new Scanner(inputStream);
        int N = in.nextInt(); // the number of points used to draw the surface of Mars.

        Coordinate landPoint = null;
        Coordinate previousCoordinate = null;
        for (int i = 0; i < N; i++) {
            int LAND_X = in.nextInt(); // X coordinate of a surface point. (0 to 6999)
            int LAND_Y = in.nextInt(); // Y coordinate of a surface point. By linking all the points together in a sequential fashion, you form the surface of Mars.

            Coordinate coordinate = new Coordinate(LAND_X, LAND_Y);
            landPoint = findLandpoint(landPoint, previousCoordinate, coordinate);
            previousCoordinate = coordinate;
        }
        assert (landPoint != null);

        LEM lem = new LEM(new Coordinate(90, 4));
        // game loop
        while (true) {

            lem.setCoordinate(new Coordinate(in.nextInt(), in.nextInt()));
            lem.setSpeed(new Speed(in.nextInt(), in.nextInt()));
            lem.setFuel(in.nextInt());      // the quantity of remaining fuel in liters.
            lem.setAngle(in.nextInt());  // the angle angle in degrees (-90 to 90).
            lem.setThrottle(in.nextInt());     // the thrust throttle (0 to 4).

            debug(lem.toString());
            debug(String.valueOf(lem.distance(landPoint, AXE_X)));

            // Handle direction
            if (lem.distanceAbs(landPoint, AXE_X) > 500) {
                debug(lem.distance(landPoint, AXE_X) > 0 ? "EST" : "WEST");
                lem.setSpeedTo(lem.distance(landPoint, AXE_X) > 0 ? EST : WEST, 15, 25);
                lem.handleAlt(landPoint);
                lem.action(out);
                continue;
            }
            if (lem.distance(landPoint, AXE_X) < 500) {
                lem.setSpeedTo(lem.distance(landPoint, AXE_X) > 0 ? EST : WEST, 0, 10);
                lem.handleAlt(landPoint);
                lem.action(out);
            }
        }
    }

    private static boolean checkBadDirection(LEM lem, Coordinate landPoint) {
        return (lem.speed.HS > 0 && lem.distance(landPoint, AXE_X) < 0) || (lem.speed.HS < 0 && lem.distance(landPoint, AXE_X) > 0);
    }

    private static void debug(String s) {
        System.err.println(s);
    }

    private static Coordinate findLandpoint(Coordinate landPoint, Coordinate previousCoordinate, Coordinate coordinate) {
        if (landPoint != null) {
            return landPoint;
        }
        if (previousCoordinate == null) {
            return null;
        }
        if (previousCoordinate.getY() == coordinate.getY()) {
            return new Coordinate(
                    (coordinate.getX() < previousCoordinate.getX() ? coordinate.getX() : previousCoordinate.getX()) + (Math.abs(coordinate.getX() - previousCoordinate.getX()) / 2)
                    , coordinate.getY());
        }
        return null;
    }

    public static class LEM {
        private Coordinate coordinate;
        private Speed speed;
        private int fuel;
        private int angle;
        private int throttle;

        public LEM(Coordinate coordinate) {
            this.coordinate = coordinate;
        }

        @Override
        public String toString() {
            return "LEM{" +
                    "coordinate=" + coordinate +
                    ", speed=" + speed +
                    ", fuel=" + fuel +
                    ", angle=" + angle +
                    ", throttle=" + throttle +
                    '}';
        }

        public Coordinate getCoordinate() {
            return coordinate;
        }

        public void setSpeed(Speed speed) {
            this.speed = speed;
        }

        public Speed getSpeed() {
            return speed;
        }

        public void setFuel(int fuel) {
            this.fuel = fuel;
        }

        public void setAngle(int angle) {
            this.angle = angle;
        }

        public void setThrottle(int throttle) {
            this.throttle = throttle;
        }

        public int getThrottle() {
            return throttle;
        }

        public int getFuel() {
            return fuel;
        }

        public int getAngle() {
            return angle;
        }

        public void setCoordinate(Coordinate coordinate) {
            this.coordinate = coordinate;
        }

        private float distance(Coordinate otherCoordinate) {
            return (float) Math.sqrt(
                    Math.pow(Math.abs((int) this.coordinate.x - (int) otherCoordinate.x), 2)
                            + Math.pow(Math.abs(this.coordinate.y - otherCoordinate.y), 2)
            );
        }

        private float distance(Coordinate otherCoordinate, int axe) {
            if (axe == AXE_X) {
                return otherCoordinate.x - this.coordinate.x;
            } else {
                return otherCoordinate.y - this.coordinate.y;
            }
        }

        public float distanceAbs(Coordinate landPoint, int axe) {
            return Math.abs(distance(landPoint, axe));
        }

        public void action(PrintStream out) {
            out.println(angle + " " + throttle);
        }

        // Speed up and speed down don't change the alt
        public void speedUp(int direction) {
            throttle = 4;
            angle = (int) Math.toDegrees(Math.cos(GRAVITY / 4));
            if (direction == EST) {
                angle = angle * -1;
            }
        }

        public void speedDown(int curDir) {
            throttle = 4;
            angle = (int) Math.toDegrees(Math.cos(GRAVITY / 4));
            if (curDir == WEST) {
                angle = angle * -1;
            }
        }

        public void setSpeedTo(int direction, int minSpeed, int maxSpeed) {
            switch (direction) {
                case EST:
                    debug("Goto EST");
                    if (speed.HS < 0) { // Bad dir
                        debug("Bad dir");
                        throttle = 4;
                        angle = -1 * (int) Math.toDegrees(Math.cos(GRAVITY / 4));
                        return;
                    }
                    if (Math.abs(speed.HS) > maxSpeed) {
                        debug("Too fast need to slow down");
                        throttle = 4;
                        angle = (int) Math.toDegrees(Math.cos(GRAVITY / 4));
                        return;
                    }
                    if (Math.abs(speed.HS) < minSpeed) {
                        debug("Too slow need to go faster");
                        throttle = 4;
                        angle = -1 * (int) Math.toDegrees(Math.cos(GRAVITY / 4));
                        return;
                    }
                    debug("Everything is OK");
                    angle = 0;
                    return;
                case WEST:
                    debug("Goto WEST");
                    if (speed.HS > 0) { // Bad dir
                        debug("Bad dir");
                        throttle = 4;
                        angle = (int) Math.toDegrees(Math.cos(GRAVITY / 4));
                        return;
                    }
                    if (Math.abs(speed.HS) > maxSpeed) {
                        debug("Too fast need to slow down");
                        throttle = 4;
                        angle = -1 * (int) Math.toDegrees(Math.cos(GRAVITY / 4));
                        return;
                    }
                    if (Math.abs(speed.HS) < minSpeed) {
                        debug("Too slow need to go faster");
                        throttle = 4;
                        angle = (int) Math.toDegrees(Math.cos(GRAVITY / 4));
                        return;
                    }
                    debug("Everything is OK");
                    angle = 0;
                    return;
            }

        }

        public void handleAlt(Coordinate landPoint) {
            if (angle == 0) {
                debug(String.valueOf(distanceAbs(landPoint, AXE_X)));
                debug(String.valueOf(distanceAbs(landPoint, AXE_Y)));
                if (distanceAbs(landPoint, AXE_X) > 500 && distanceAbs(landPoint, AXE_Y) < 1000 && speed.VS < 4) {
                    throttle = 4;
                    return;
                }
                if (Math.abs(speed.VS) > 38) {
                    throttle = 4;
                } else {
                    throttle = 3;
                }
            }
        }
    }

    public static class Coordinate {
        private final float x;
        private final float y;

        public Coordinate(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Coordinate{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }

    public static class Speed {
        private final float HS;
        private final float VS;

        public Speed(float HS, float VS) {
            this.HS = HS;
            this.VS = VS;
        }

        @Override
        public String toString() {
            return "Speed{" +
                    "HS=" + HS +
                    ", VS=" + VS +
                    '}';
        }

        public float getHS() {
            return HS;
        }

        public float getVS() {
            return VS;
        }
    }
}
