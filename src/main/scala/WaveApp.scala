import java.util.Calendar

import akka.actor.ActorSystem
import pipeline.in.CsvActor
import pipeline.model.EsClusterAddress
import pipeline.out.EsActor
import scala.io.Source

object WaveApp extends App {
  val system = ActorSystem("test")

  val source = Source.fromFile("test.csv")
  val cluster = EsClusterAddress("elasticsearch", List(
    ("localhost", 9300)
  ))

  val next = system.actorOf(EsActor.props("test", "foo", cluster), "out")
  val in = system.actorOf(CsvActor.props(source, next), "in")

  in ! CsvActor.Start

  system.awaitTermination()
}