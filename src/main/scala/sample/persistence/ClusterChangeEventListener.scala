package sample.persistence

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, MemberUp, UnreachableMember}

class ClusterChangeEventListener extends Actor with ActorLogging {

  val cluster = Cluster(context.system)


  override def preStart(): Unit = {
    cluster.subscribe(self,
      InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = {
    case MemberUp(member) =>
      log.info("New member in cluster: {}", member.address)

    case UnreachableMember(member) =>
      log.info("Member is unreachable: {}", member)
  }
}
