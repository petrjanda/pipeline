import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.scaladsl._
import pipeline.model.EsClusterAddress
import pipeline.streams.{EsIndex, CsvParser}

object StreamApp extends App {
  implicit val system = ActorSystem("Sys")
  implicit val materializer = FlowMaterializer()

  import system.dispatcher

  // Configuration
  val lines = io.Source.fromFile("insurance.csv").getLines()
  val headersLine = lines.next
  val cluster = EsClusterAddress("elasticsearch",
    List("localhost" -> 9300)
  )

  // Console sink
  val consoleSink = Sink.foreach(println)

  // Flow
  val flow = FlowGraph { implicit builder =>
    import FlowGraphImplicits._

    val csvParser = CsvParser().flow(headersLine)
    val esIndex = EsIndex(cluster).flow[String]("test/test")

    Source(lines.toStream) ~> csvParser ~> esIndex ~> consoleSink
  }.run()

  // Terminate
  flow.get(consoleSink).onComplete {
    case _ => system.shutdown()
  }
}