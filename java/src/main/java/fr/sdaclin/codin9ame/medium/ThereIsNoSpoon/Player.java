package fr.sdaclin.codin9ame.medium.ThereIsNoSpoon;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Don't let the machines win. You are humanity's last hope...
 **/
class Player {

  public static void main(String args[]) {
    Scanner in = new Scanner(System.in);
    int width = in.nextInt(); // the number of cells on the X axis
    int height = in.nextInt(); // the number of cells on the Y axis
    in.nextLine();
    Grid grid = new Grid(width, height);
    for (int y = 0; y < height; y++) {
      String line = in.nextLine(); // width characters, each either 0 or .
      for (int x = 0; x < width; x++) {
        if (line.substring(x, x + 1).equals("0")) {
          grid.addNode(new Node(x, y));
        }
      }
    }

    grid.listAllNodes().forEach(node -> {
      System.out.println(node + " " + grid.getRightNode(node).orElse(Node.EMPTY) + " " + grid.getBottomNode(node).orElse(Node.EMPTY));
    });
  }

  static class Grid {
    private final Node[][] nodes;
    private final int width;
    private final int height;

    Grid(int width, int height) {
      nodes = new Node[width][height];
      this.width = width;
      this.height = height;
    }

    void addNode(Node node) {
      nodes[node.x][node.y] = node;
    }

    List<Node> listAllNodes() {
      return Stream.of(nodes).flatMap(col -> Stream.of(col).filter(cell -> cell != null)).collect(Collectors.toList());
    }

    Optional<Node> getRightNode(Node node) {
      for (int i = node.x + 1; i < width; i++) {
        if (nodes[i][node.y] != null)
          return Optional.of(nodes[i][node.y]);
      }
      return Optional.empty();
    }

    Optional<Node> getBottomNode(Node node) {
      for (int i = node.y + 1; i < height; i++) {
        if (nodes[node.x][i] != null)
          return Optional.of(nodes[node.x][i]);
      }
      return Optional.empty();
    }
  }

  static class Node {
    static final Node EMPTY = new Node(-1, -1);
    private final int x;
    private final int y;

    Node(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public String toString() {
      return x + " " + y;
    }
  }
}
