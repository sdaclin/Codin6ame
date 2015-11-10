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
    private static final String LVL_8 = "8 8\n" +
            "3.4.6.2.\n" +
            ".1......\n" +
            "..2.5..2\n" +
            "1.......\n" +
            "..1.....\n" +
            ".3..52.3\n" +
            ".2.17..4\n" +
            ".4..51.2";

    @DataProvider(name = "fromWebSiteDataProvider")
    public Object[][] createData() {
        return new Object[][]{
                //{LVL_1},
                //{LVL_3},
                //{LVL_4},
                //{LVL_6},
                {LVL_8},
        };
    }

    @Test(dataProvider = "fromWebSiteDataProvider")
    public void testAllLevelFromWebsite(String lvlConfig) throws Exception {
        Player.Configuration configuration = Player.Configuration.forDump(lvlConfig);
        Player.Solver solver = new Player.Solver(configuration);
        //List<Player.Solver.Connection> connections = solver.solve();
        solver.printResult();
    }

    class SimpleLine implements Player.Solver.Line {
        private final Player.Solver.Coordinate a;
        private final Player.Solver.Coordinate b;
        private final Orientation orientation;

        SimpleLine(int ax, int ay, int bx, int by){
            this.a = new Player.Solver.Coordinate(ax,ay);
            this.b = new Player.Solver.Coordinate(bx,by);
            this.orientation = (ax == bx ? Orientation.VERTICAL : Orientation.HORIZONTAL);
        }

        @Override
        public Player.Solver.Coordinate getCoordinateOfA() {
            return this.a;
        }

        @Override
        public Player.Solver.Coordinate getCoordinateOfB() {
            return this.b;
        }

        @Override
        public Orientation getOrientation() {
            return orientation;
        }
    }

    @DataProvider(name="intersectionsDataProvider")
    public Object[][] createDataForIntersection(){
        return new Object[][]{
                {new SimpleLine(0,0,2,0),new SimpleLine(1,0,1,1),false},
                {new SimpleLine(1,0,1,2),new SimpleLine(0,1,2,1),true},
                {new SimpleLine(0,1,2,1),new SimpleLine(1,0,1,2),true}

        };
    }

    @Test(dataProvider = "intersectionsDataProvider")
    public void testIntersects(Player.Solver.Line lineA, Player.Solver.Line lineB, Boolean expectation){
        assert(Player.Solver.intersects(lineA,lineB)==expectation);
    }
}