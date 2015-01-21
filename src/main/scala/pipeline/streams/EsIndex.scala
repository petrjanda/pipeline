package pipeline.streams

import akka.stream.javadsl.PartialFlowGraph
import akka.stream.javadsl.PartialFlowGraph
import akka.stream.scaladsl._
import com.sksamuel.elastic4s.ElasticClient
import org.elasticsearch.common.settings.ImmutableSettings
import pipeline.model.{DataRow, EsClusterAddress}

/**
 * Created by petr on 20/01/2015.
 */
case class EsIndex(cluster: EsClusterAddress) {
  val settings = ImmutableSettings.settingsBuilder()
    .put("http.enabled", false)
    .put("cluster.name", cluster.name)

  val client = ElasticClient.remote(settings.build(), cluster.hosts: _*)

  def flow[T](indexAs:String, batchSize:Int = 100) = Flow() { implicit builder =>
    import akka.stream.scaladsl.FlowGraphImplicits._

    val in = UndefinedSource[DataRow[T]]
    val out = UndefinedSink[Seq[DataRow[T]]]

    val batchBuilder = Flow[DataRow[T]].grouped(batchSize)

    val indexMapper = Flow[Seq[DataRow[T]]].map(rows => {
      import com.sksamuel.elastic4s.ElasticDsl._

      val definitions = rows.toList.map(row => {
        index into indexAs fields row.data
      })

      client.execute {
        bulk(definitions: _*)
      }.await

      rows
    })

    in ~> batchBuilder ~> indexMapper ~> out

    in -> out
  }

  def close = {
    client.close()
  }
}
