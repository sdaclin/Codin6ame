package fr.sdaclin.codin6ame.hard.APUImprovementPhase;

import org.testng.annotations.Test;

public class PlayerTest {

    @Test
    public void lvl1() throws Exception {
        String lvl1 = "3 3\n" +
                "1.2\n" +
                "...\n" +
                "..1";
        Player.Configuration configuration = Player.Configuration.forDump(lvl1);
        Player.Solver solver = new Player.Solver(configuration);
        solver.solve();
    }
}