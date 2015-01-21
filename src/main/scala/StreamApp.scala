import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.scaladsl._
import au.com.bytecode.opencsv.CSVParser
import pipeline.model.{DataRow, EsClusterAddress}
import pipeline.streams.{EsIndexFlowFactory, CsvParserFlowFactory}


object StreamApp extends App {
  implicit val system = ActorSystem("Sys")
  implicit val materializer = FlowMaterializer()

  import system.dispatcher

  // Config
  val file = io.Source.fromFile("insurance.csv")
  val cluster = EsClusterAddress("elasticsearch",
    List("localhost" -> 9300)
  )

  val headers = file.getLines().next()

  // Flows
  val source = Source(file.getLines().toStream)

  val csvParser = CsvParserFlowFactory().flow(headers)

  val indexFactory = EsIndexFlowFactory[String](cluster)
  val esIndexer = indexFactory.flow("foo/bar")

  val console = Sink.foreach[Any](item => print('.'))

  val flow = FlowGraph { implicit builder =>
    import FlowGraphImplicits._



    source ~> csvParser ~> esIndexer ~> console
  }.run()

  flow.get(console).onComplete { _ =>
    indexFactory.close
    system.shutdown()
  }
}
