package fr.sdaclin.codin9ame.hard.IndianaLevel2;

import org.testng.annotations.Test;

public class PlayerTest {

    @Test
    public void testLvl1() {
        Player.Configuration configuration = Player.Configuration.forDump("5 3\n" +
                "0 0 -3 0 0\n" +
                "0 0 2 0 0\n" +
                "0 0 -3 0 0\n" +
                "2");
        Player.Solver solver = new Player.Solver(configuration);
        solver.setIndyState(new Player.Coordinate(2, 0), "TOP");
        while (solver.printNextCommand()) {
        }
        ;
    }

    @Test
    public void testLvl2() {
        Player.Configuration configuration = Player.Configuration.forDump("8 4\n" +
                "0 -3 0 0 0 0 0 0\n" +
                "0 12 3 3 2 3 12 0\n" +
                "0 0 0 0 0 0 2 0\n" +
                "0 -12 3 2 2 3 13 0\n" +
                "1");
        Player.Solver solver = new Player.Solver(configuration);
        solver.setIndyState(new Player.Coordinate(1, 0), "TOP");
        while (solver.printNextCommand()) {
        }
        ;
    }
}