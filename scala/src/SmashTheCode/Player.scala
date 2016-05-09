package SmashTheCode

import scala.io.Source

object Player extends App {
  val emptyCell = Cell(".")
  val emptyCol = Col(Nil)
  val emptyGrid = Grid(List(emptyCol, emptyCol, emptyCol, emptyCol, emptyCol, emptyCol))

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
    var myGrid = emptyGrid
    for (i <- 0 until 12) {
      val row = readALine
      myGrid = myGrid.read(row)
    }
    var theirGrid = emptyGrid
    for (i <- 0 until 12) {
      val row = readALine // One line of the map ('.' = empty, '0' = skull block, '1' to '5' = colored block)
      theirGrid = theirGrid.read(row)
    }

    val nextBlock = stack.next()
    val position = myGrid.findTopMatchingColorIdx(nextBlock.cellDown.content)
      .getOrElse(myGrid.findAPositionWithAdjacentColor(nextBlock.cellDown.content)
        .getOrElse(nextBlock.cellDown.content.toInt)
      )

    println(position) // "x": the column in which to drop your blocks
  }

  def debug(message: String) = Console.err.println(message)


  case class Stack(content: List[Block]) {
    def next() = content.head

    def add(block: Block): Stack = {
      Stack(content ::: block :: Nil)
    }

    def get(block: Block): (Block, Stack) = {
      (content.head, Stack(content.tail))
    }
  }

  case class Cell(content: String) {}

  case class Col(content: List[Cell]) {
    def put(value: Cell): Col = {
      Col(content ::: value :: Nil)
    }

    def head: Cell = {
      content.head
    }

    def isEmpty = {
      content.isEmpty
    }

    def height = {
      content.length
    }

    def getContentAtHeight(height: Int): Option[Cell] = {
      if (content.size < height) {
        None
      }
      Some(content(height))
    }
  }

  case class Grid(content: List[Col]) {
    def read(line: String): Grid = {
      var newGrid = this
      val lineContent: Array[String] = line.split("")
      for (i <- lineContent.indices) {
        if (lineContent(i) != ".") {
          newGrid = newGrid.put(i, Cell(lineContent(i)))
        }
      }
      newGrid
    }

    def put(colIdx: Int, cell: Cell): Grid = {
      Grid(content.updated(colIdx, content(colIdx).put(cell)))
    }

    def findTopMatchingColorIdx(color: String): Option[Int] = {
      for (i <- content.indices) {
        if (!content(i).isEmpty && content(i).head.content == color) {
          return Some(i)
        }
      }
      None
    }

    def getCellAt(x:Int,y:Int):Option[Cell] = {
      if (x<0 || x >= content.size){
        return None
      }
      if (y >= content(x).content.size){
        return None
      }
      Some(content(x).content(y))
    }

    def findAPositionWithAdjacentColor(color: String): Option[Int] = {
      for (i <- content.indices) {
        val currentHeight = content(i).height
        if (getCellAt(i-1,currentHeight).getOrElse(emptyCell).content == color) {
          return Some(i)
        } else if (getCellAt(i+1,currentHeight).getOrElse(emptyCell).content == color) {
          return Some(i)
        }
      }
      None
    }
  }

  case class Block(cellUp: Cell, cellDown: Cell)

}
