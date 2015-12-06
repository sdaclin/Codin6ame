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
    }
}