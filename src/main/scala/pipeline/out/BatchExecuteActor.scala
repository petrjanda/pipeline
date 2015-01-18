package pipeline.out

import akka.actor._
import com.sksamuel.elastic4s.{BulkCompatibleDefinition, ElasticClient}
import com.sksamuel.elastic4s.ElasticDsl._
import org.elasticsearch.common.settings.ImmutableSettings
import pipeline.model.EsClusterAddress
import scala.collection.mutable


object BatchExecuteActor {
  case class Execute(batch:List[BulkCompatibleDefinition])

  case object Executed

  case object Close

  def props(cluster:EsClusterAddress) = Props(new BatchExecuteActor(cluster))
}

class BatchExecuteActor(cluster:EsClusterAddress) extends Actor with ActorLogging {
  import BatchExecuteActor._

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    self forward message
  }

  val settings = ImmutableSettings.settingsBuilder()
    .put("http.enabled", false)
    .put("cluster.name", cluster.name)

  val client = ElasticClient.remote(settings.build(), cluster.hosts:_*)

  val queue = mutable.Queue[Execute]()

  def busy:Receive = {
    case execute:Execute => queue.enqueue(execute)
  }

  def idle:Receive = {
    case Execute(batch) => {
      import context.dispatcher

      context.become(busy)

      val f = client.execute { bulk(batch: _*) }
      val c = context

      f.onComplete(resp => {
        c.become(idle)

        c.parent ! Executed

        if(queue.nonEmpty) {
          self forward queue.dequeue()
        }
      })
    }
  }

  def receive = idle
}