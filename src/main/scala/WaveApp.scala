import akka.actor.ActorSystem
import pipeline.in.CsvActor
import pipeline.model.EsClusterAddress
import pipeline.out.EsActor
import scala.io.Source

object TestApp extends App {
  val system = ActorSystem("test")

  // Output
  val cluster = EsClusterAddress(
    name =  "elasticsearch",
    hosts = List(("localhost", 9300))
  )

  val next = system.actorOf(EsActor.props("test", "foo", cluster), "out")

  // Input
  val source = Source.fromFile("test.csv")
  val in = system.actorOf(CsvActor.props(source, next), "in")

  in ! CsvActor.Start

  system.awaitTermination()
}