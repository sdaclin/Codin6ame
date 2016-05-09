package SmashTheCode

object PlayerTest extends App {
  testFile()

  def testFile(): Unit = {
    Player.main(Array("test.txt"))
  }
}
