package pipeline.in

import _root_.pipeline.in.CsvActor.InvalidLine
import _root_.pipeline.model.DataRow
import pipeline.model.DataRow
import akka.actor.{Props, Actor, ActorRef}
import au.com.bytecode.opencsv.CSVParser

object CsvLineActor {
  case object Start
  case object Stop
  case class Parse(line:String)
  case object GetLine

  def props(next:ActorRef, headers:List[String]) = Props(new CsvLineActor(next, headers))
}

class CsvLineActor(next:ActorRef, headers:List[String]) extends Actor {
  import CsvLineActor._

  val p = new CSVParser(',')

  def active:Receive = {
    case Stop => context.become(inactive)

    case Parse(line) => {
      val values = parseLine(line)

      next ! buildDataRow(values)
      sender() ! GetLine
    }
  }

  def inactive:Receive = {
    case Start => {
      context.become(active)

      sender() ! GetLine
    }
  }

  def receive = inactive

  protected def buildDataRow(values:List[String]) = {
    DataRow[String](headers.zip(values).toMap)
  }

  protected def parseLine(line:String):List[String] = {
    try {
      p.parseLine(line).toList
    } catch {
      case e:Throwable => throw InvalidLine(line)
    }
  }
}