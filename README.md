# Scala Data Processing Pipeline

High performance parallelized data transformation processing pipeline.

# Supported data formats

At the moment, the focus is on columnar data (in Scala represented as `Map[String, Any]`.

## Input

[x] CSV file with headers
[x] CSV file without headers
[ ] JSON records file (one JSON value per line)

## Output

[x] ElasticSearch index

# Usage

```scala
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
```
