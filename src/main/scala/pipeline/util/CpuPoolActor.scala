package pipeline.util

import akka.actor.{ActorContext, Props}
import akka.routing.BalancingPool
import pipeline.in.CsvLineActor

trait CpuPoolActor {
  val cpus = Runtime.getRuntime.availableProcessors()

  def balancingPool(props:Props) = {
    BalancingPool(cpus).props(props)
  }

  def listPool(props:Props, context:ActorContext) = {
    1.to(cpus).toList.map(i => {
      context.actorOf(props, "worker-" + i)
    })
  }
}