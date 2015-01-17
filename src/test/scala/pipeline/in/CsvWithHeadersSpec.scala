package pipeline.in

import java.util.concurrent.TimeUnit

import _root_.pipeline.in.pipeline.model.DataRow
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike, MustMatchers}
import scala.io.Source
import scala.concurrent.duration.FiniteDuration

class CsvWithHeadersSpec(_system:ActorSystem) extends TestKit(_system) with ImplicitSender with MustMatchers with WordSpecLike with BeforeAndAfterAll {
  def this() = this(ActorSystem("MySpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "CSV actor" must {
    "parse the lines" in {
      val source = Source.fromURL(getClass.getResource("/fixtures/list.csv"))
      val c = system.actorOf(CsvWithHeadersActor.props(source))

      c ! CsvActor.Parse(self)

      val row = DataRow[String](
        Map("foo" -> "1", "bar" -> "2", "baz" -> "3,")
      )

      expectMsg(row)
    }

    "parse invalid lines" in {
      val source = Source.fromURL(getClass.getResource("/fixtures/invalid.csv"))
      val c = system.actorOf(CsvWithHeadersActor.props(source))

      c ! CsvActor.Parse(self)

      val row = DataRow[String](
        Map("foo" -> "1", "bar" -> "2", "baz" -> "3,")
      )


      expectNoMsg(FiniteDuration(100, TimeUnit.MILLISECONDS))
    }
  }
}
