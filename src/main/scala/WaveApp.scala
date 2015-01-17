import java.util.Calendar

import akka.actor.{Actor, Props, ActorSystem}
import pipeline.in.pipeline.model.DataRow
import pipeline.in.CsvActor

import scala.collection.mutable.ListBuffer
import scala.io.Source

object WaveApp extends App {
  val system = ActorSystem("test")

  val source = Source.fromFile("pp-2014.csv")
  val next = system.actorOf(Props[OutActor], "out")
  val in = system.actorOf(CsvActor.props(source, next), "in")

  in ! CsvActor.Start

  system.awaitTermination()
}

class OutActor extends Actor {
  var i = 0
  val time = Calendar.getInstance().getTimeInMillis

  val prices = ListBuffer[Int]()

  def receive = {
    case row:DataRow[_] => {
      i += 1

      prices += row.data("180000").asInstanceOf[String].toInt

      if(i % 10000 == 0) {
        println(i)
      }
    }

    case CsvActor.Done => {
      println(prices.find(_ > 10000000))

      context.system.shutdown()
    }
  }
}