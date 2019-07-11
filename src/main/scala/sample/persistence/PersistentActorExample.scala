package sample.persistence

//#persistent-actor-example
import akka.actor._
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import akka.persistence._
import akka.persistence.journal.Tagged
import akka.persistence.journal.leveldb.{SharedLeveldbJournal, SharedLeveldbStore}
import sample.persistence.ExamplePersistentActor.{idExtractor, props, shardResolver}

import scala.io.StdIn

case class Cmd(data: String)

case class Evt(data: String)

case class ExampleState(events: List[String] = Nil) {
  def updated(evt: Evt): ExampleState = copy(evt.data :: events)

  def size: Int = events.length

  override def toString: String = events.reverse.toString
}

class ExamplePersistentActor extends PersistentActor {
  override def persistenceId = "PersistentActor"

  var state = ExampleState()

  def updateState(event: Evt): Unit =
    state = state.updated(event)

  def numEvents =
    state.size

  val receiveRecover: Receive = {
    case evt: Evt => {
      println(s"Received event data : ${evt.data} while recovering")
      updateState(evt)
    }
    case SnapshotOffer(_, snapshot: ExampleState) => {
      println("You know what? I got a snapshot, so I can now recover faster! Thanks ancestor! 🙇️")
      state = snapshot
    }
  }

  val receiveCommand: Receive = {
    case Cmd(data) =>
      println(s"Trying to persist : ${data}")
      persist(Tagged(Evt(s"${data}-${numEvents}"), Set(s"Pahila manus parat - $data"))) { tagged =>
        println(s"Persisted : ${tagged}")
        updateState(tagged.payload.asInstanceOf[Evt])
        context.system.eventStream.publish(tagged)
      }
    case "snap" => saveSnapshot(state)
    case "print" => println(s"Current state : $state")
  }

}

object ExamplePersistentActor {
  def props = Props(new ExamplePersistentActor())

  def idExtractor: ShardRegion.ExtractEntityId = {
    case c: Cmd => ("PersistentActor", c)
    case s: String => ("PersistentActor", s)
  }

  def shardResolver: ShardRegion.ExtractShardId = /*case c: Cmd => (c.data.hashCode % 2).toString
    case s: String => (s.hashCode % 2).toString*/ _ => "FooBar"
}

//#persistent-actor-example

object PersistentActorExample extends App {

  val system = ActorSystem("ClusterSystem")

  val store: ActorRef = system.actorOf(Props[SharedLeveldbStore], "store")

  SharedLeveldbJournal.setStore(store, system)

  val persistentActor = ClusterSharding(system).start(
    typeName = "PersistentActor",
    entityProps = props,
    settings = ClusterShardingSettings(system),
    extractEntityId = idExtractor,
    extractShardId = shardResolver)


  def sleepNWaky = {
    println("Sleeping now... 😵")
    Thread.sleep(1000)
    println("Waking up now... 😳")
  }

  for (i <- 35000 to 36000) {
    persistentActor ! Cmd(i.toString + "pahila parat")
    sleepNWaky
  }

  persistentActor ! "print"

  StdIn.readLine
}
