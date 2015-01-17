package pipeline.util

import akka.actor.ActorRef

/**
 * Created by petr on 17/01/2015.
 */
class WorkersSet {
  var set = Set[String]()

  def ++(refs:List[ActorRef]) = set ++= refs.map(_.path.name)

  def -(ref:ActorRef):WorkersSet = {
    set -= ref.path.name

    this
  }

  def isEmpty = set.isEmpty
}
