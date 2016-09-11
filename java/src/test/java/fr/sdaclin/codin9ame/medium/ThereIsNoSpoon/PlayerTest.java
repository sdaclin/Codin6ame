package fr.sdaclin.codin9ame.medium.ThereIsNoSpoon;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class PlayerTest {
  @Test
  public void testGrid() {
    // 0.0
    // .0.
    // 0..
    Player.Grid grid = new Player.Grid(3, 3);
    Player.Node topLeftNode = new Player.Node(0, 0);
    Player.Node topRightNode = new Player.Node(2, 0);
    Player.Node bottomLeftNode = new Player.Node(0, 2);
    Player.Node centerNode = new Player.Node(1, 1);

    grid.addNode(topLeftNode);
    grid.addNode(topRightNode);
    grid.addNode(bottomLeftNode);
    grid.addNode(centerNode);

    assertEquals(grid.listAllNodes().size(), 4);
    assertEquals(grid.getRightNode(topLeftNode).get(), topRightNode);
    assertEquals(grid.getBottomNode(topLeftNode).get(), bottomLeftNode);
  }
}