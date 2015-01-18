package pipeline.out

import akka.actor.SupervisorStrategy.{Resume, Escalate}
import akka.actor.{OneForOneStrategy, Actor, ActorLogging, Props}
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.IndexDefinition
import pipeline.in.CsvActor.Done
import pipeline.model.{EsClusterAddress, DataRow}

object EsActor {
  def props(index:String, as:String, cluster:EsClusterAddress):Props = Props(new EsActor(index, as, cluster))
}

class EsActor(indexName:String, as:String, cluster:EsClusterAddress) extends Actor with ActorLogging {
  override val supervisorStrategy = OneForOneStrategy() {
    case _ => Escalate
  }

  var batch = context.actorOf(BatchActor.props(500, cluster), "batch")

  def receive = {
    case DataRow(data) => {
      val definition:IndexDefinition = index into(indexName + as) fields data

      batch ! BatchActor.Enqueue(definition)
    }

    case Done => {
      batch ! BatchActor.Close
    }
  }
}



