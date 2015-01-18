# Scala Data Processing Pipeline

High performance parallelized data transformation processing pipeline.

# Supported data formats

At the moment, the focus is on columnar data (in Scala represented as `Map[String, Any]`.

## Input

- [x] CSV file with headers
- [x] CSV file without headers
- [ ] JSON records file (one JSON value per line)

## Output

- [x] ElasticSearch index

# Usage

To demonstrate the easiest use case, the following example does perform simple ETL-like data transformation taking CSV file as an input and indexing it to the ElasticSearch `test` index as documents of type `foo`.

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

  val out = system.actorOf(EsActor.props("test", "foo", cluster), "out")

  // Input
  val source = Source.fromFile("test.csv")
  val in = system.actorOf(CsvActor.props(source, out), "in")

  in ! CsvActor.Start

  system.awaitTermination()
}
```

Using the Akka actors to perform individual steps of the processing, one is able to plugin intermediate step, which can do any type of transformation (mapping, reducing, data types manipulation, etc.).
