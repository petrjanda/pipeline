package pipeline.in

import _root_.pipeline.in.CsvActor.Done
import _root_.pipeline.util.{WorkersSet, CpuPoolActor}
import akka.actor.SupervisorStrategy.{Escalate}
import akka.actor._
import au.com.bytecode.opencsv.CSVParser

import scala.io.Source

object CsvActor {
  case object Start

  case object Done

  case class InvalidLine(line:String) extends RuntimeException("Invalid line: " + line)

  def props(source:Source, next:ActorRef):Props = {
    val iterator = source.getLines()
    val headers = new CSVParser(',').parseLine(iterator.next()).toList

    props(source, headers, next)
  }

  def props(source:Source, headers:List[String], next:ActorRef):Props = Props(
    new CsvActor(source.getLines(), headers, next)
  )

  private def parseLine(line:String):List[String] = {
    new CSVParser(',').parseLine(line).toList
  }
}

class CsvActor(lines:Iterator[String], headers:List[String], next:ActorRef) extends Actor with CpuPoolActor {
  override val supervisorStrategy = OneForOneStrategy() { case _ => Escalate }

  val workers = listPool(CsvLineActor.props(next, headers), context)
  val working = new WorkersSet()

  def receive = {
    case CsvActor.Start => {
      context.actorSelection("*") ! CsvLineActor.Start
      working ++ workers
    }

    case CsvLineActor.GetLine => {
      if(lines.hasNext) {
        sender() ! CsvLineActor.Parse(lines.next)
      } else {
        if((working - sender()).isEmpty) next ! Done
      }
    }
  }
}