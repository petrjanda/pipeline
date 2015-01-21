package pipeline.out

import java.util.Calendar

import akka.actor.SupervisorStrategy.{Restart, Escalate}
import akka.actor._
import com.sksamuel.elastic4s.{BulkCompatibleDefinition, ElasticClient, IndexDefinition}
import com.sksamuel.elastic4s.ElasticDsl._
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.common.settings.ImmutableSettings
import pipeline.model.EsClusterAddress
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

private object BatchActor {
  case class Enqueue(definition:IndexDefinition)

  case object Close

  def props(size:Int, cluster:EsClusterAddress):Props = Props(new BatchActor(size, cluster))
}

private class BatchActor(size:Int, cluster:EsClusterAddress) extends Actor with ActorLogging {
  override val supervisorStrategy = OneForOneStrategy(10000) {
    case e:java.util.concurrent.TimeoutException => Restart
    case e:org.elasticsearch.client.transport.NoNodeAvailableException => Restart
    case _ => Escalate
  }

  import BatchActor._
  import context._

  private val list:ListBuffer[IndexDefinition] = ListBuffer()

  val actor = actorOf(BatchExecuteActor.props(cluster))

  var runningCommits = 0

  def closing:Receive = {
    case Enqueue(definition) => {
      throw new RuntimeException("Closed!")
    }

    case BatchExecuteActor.Executed => {
      runningCommits -= 1

      if(runningCommits == 0) {
        actor ! BatchExecuteActor.Close

        Thread.sleep(1000)

        context.system.shutdown()
      }
    }
  }

  def receive = {
    case Enqueue(definition) => {
      list.append(definition)

      if(list.length == size) {
        commit()
      }
    }

    case Close => {
      become(closing)

      commit(isClosing = true)
    }

    case BatchExecuteActor.Executed => {
      runningCommits -= 1
    }
  }

  private def commit(isClosing:Boolean = false) = {
    actor ! BatchExecuteActor.Execute(list.toList)

    runningCommits += 1

    list.clear()
  }
}



