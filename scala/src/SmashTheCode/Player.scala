package SmashTheCode

import scala.io.Source

object Player extends App {
  val emptyCell = Cell(".")
  val emptyRow = Row(List(emptyCell, emptyCell, emptyCell, emptyCell, emptyCell, emptyCell))
  val emptyGrid = Grid(List(emptyRow, emptyRow, emptyRow, emptyRow, emptyRow, emptyRow, emptyRow, emptyRow, emptyRow, emptyRow, emptyRow, emptyRow))

  val testFileContent: Iterator[String] = if (args.length == 1) {
    Source.fromFile(args(0)).getLines()
  } else {
    Iterator.empty
  }

  def readALine: String =
    if (args.length == 1) {
      testFileContent.next()
    } else {
      scala.io.StdIn.readLine()
    }

  // game loop
  while (true) {
    var stack = Stack(Nil)

    for (i <- 0 until 8) {
      // colora: color of the first block
      // colorb: color of the attached block
      val colors = for (i <- readALine split " ") yield i
      stack = stack.add(Block(Cell(colors(0)), Cell(colors(1))))
    }
    val myGrid = emptyGrid
    for (i <- 0 until 12) {
      val row = readALine
      myGrid.read(i, row)
    }
    val theirGrid = emptyGrid
    for (i <- 0 until 12) {
      val row = readALine // One line of the map ('.' = empty, '0' = skull block, '1' to '5' = colored block)
      theirGrid.read(i, row)
    }
    // Write an action using println
    // To debug: Console.err.println("Debug messages...")

    println("0") // "x": the column in which to drop your blocks
  }

  def debug(message: String) = Console.err.println(message)



  case class Stack(content: List[Block]) {
    def add(block: Block): Stack = {
      Stack(content ::: block :: Nil )
    }

    def get(block: Block): (Block, Stack) = {
      (content.head, Stack(content.tail))
    }
  }

  case class Cell(content: String) {}

  case class Row(content: List[Cell]) {
    def changeCell(idx: Int, value: Cell): Row = {
      Row(content.updated(idx, value))
    }
  }

  case class Grid(content: List[Row]) {
    def changeRow(idx: Int, value: Row): Grid = {
      Grid(content.updated(idx, value))
    }

    def read(idx: Int, line: String): Grid = {
      changeRow(idx, Row(line.split("").map(char => Cell(char)).toList))
    }
  }

  case class Block(cellUp: Cell, cellDown: Cell)

}
