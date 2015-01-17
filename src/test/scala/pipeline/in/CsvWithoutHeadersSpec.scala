package pipeline.in

import _root_.pipeline.in.pipeline.model.DataRow
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike, MustMatchers}
import scala.io.Source

class CsvWithoutHeadersSpec(_system:ActorSystem) extends TestKit(_system) with ImplicitSender with MustMatchers with WordSpecLike with BeforeAndAfterAll {
  def this() = this(ActorSystem("MySpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "CsvWithoutHeaders" must {
    "parse the lines" in {
      val source = Source.fromURL(getClass.getResource("/fixtures/noheaders.csv"))
      val c = system.actorOf(CsvWithoutHeadersActor.props(source, List("foo", "bar", "baz")))

      c ! CsvActor.Parse(self)

      expectMsg(DataRow[String](
        Map("foo" -> "1", "bar" -> "2", "baz" -> "3,")
      ))

      expectMsg(DataRow[String](
        Map("foo" -> "11", "bar" -> "12", "baz" -> "13,")
      ))
    }
  }
}
