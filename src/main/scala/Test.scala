import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._

object Test extends App {

  val client = ElasticClient.remote("localhost", 9300)

  // await is a helper method to make this operation sync instead of async
  // You would normally avoid doing this in a real program as it will block
  client.execute { index into "bands/artists" fields "name"->"coldplay" }.await

  val resp = client.execute { search in "bands/artists" query "coldplay" }.await
  println(resp)

  client.close()
}