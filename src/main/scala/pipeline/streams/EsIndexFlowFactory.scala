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
case class EsIndexFlowFactory[T](cluster: EsClusterAddress) {
  type Data = DataRow[T]

  val settings = ImmutableSettings.settingsBuilder()
    .put("http.enabled", false)
    .put("cluster.name", cluster.name)

  val client = ElasticClient.remote(settings.build(), cluster.hosts: _*)

  def flow(indexAs:String) = Flow() { implicit builder =>
    import akka.stream.scaladsl.FlowGraphImplicits._

    val undefinedSource = UndefinedSource[Data]
    val undefinedSink = UndefinedSink[Seq[Data]]

    val grouper = Flow[Data].grouped(100)

    val indexMapper = Flow[Seq[Data]].map(rows => {
      import com.sksamuel.elastic4s.ElasticDsl._

      val definitions = rows.toList.map(row => {
        index into indexAs fields row.data
      })

      client.execute {
        bulk(definitions: _*)
      }.await

      rows
    })

    undefinedSource ~> grouper ~> indexMapper ~> undefinedSink

    undefinedSource -> undefinedSink
  }

  def close = {
    client.close()
  }
}
