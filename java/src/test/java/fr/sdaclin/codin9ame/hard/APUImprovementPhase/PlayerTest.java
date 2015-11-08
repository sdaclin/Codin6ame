package fr.sdaclin.codin9ame.hard.APUImprovementPhase;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PlayerTest {

    private static final String LVL_1 = "3 3\n" +
            "1.2\n" +
            "...\n" +
            "..1";
    private static final String LVL_3 = "3 3\n" +
            "1.3\n" +
            "...\n" +
            "123";
    private static final String LVL_4 = "4 3\n" +
            "14.3\n" +
            "....\n" +
            ".4.4";
    private static final String LVL_6 = "7 5\n" +
            "2..2.1.\n" +
            ".3..5.3\n" +
            ".2.1...\n" +
            "2...2..\n" +
            ".1....2";

    @DataProvider(name = "fromWebSiteDataProvider")
    public Object[][] createData() {
        return new Object[][]{
                //{LVL_1},
                //{LVL_3},
                //{LVL_4},
                {LVL_6},
        };
    }

    @Test(dataProvider = "fromWebSiteDataProvider")
    public void testAllLevelFromWebsite(String lvlConfig) throws Exception {
        Player.Configuration configuration = Player.Configuration.forDump(lvlConfig);
        Player.Solver solver = new Player.Solver(configuration);
        //List<Player.Solver.Connection> connections = solver.solve();
        solver.printResult();
    }
}