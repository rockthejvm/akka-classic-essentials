package part5infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.io.Udp.SO
import akka.routing._
import com.typesafe.config.ConfigFactory

object Routers extends App {

  /**
    * #1 - manual router
   */
  class Master extends Actor {
    // step 1 - create routees
    // 5 actor routees based off Slave actors
    private val slaves = for(i <- 1 to 5) yield {
      val slave = context.actorOf(Props[Slave], s"slave_$i")
      context.watch(slave)
      ActorRefRoutee(slave)
    }

    // step 2 - define router
    private val router = Router(RoundRobinRoutingLogic(), slaves)

    override def receive: Receive = {
      // step 4 - handle the termination/lifecycle of the routees
      case Terminated(ref) =>
        router.removeRoutee(ref)
        val newSlave = context.actorOf(Props[Slave])
        context.watch(newSlave)
        router.addRoutee(newSlave)
      // step 3 - route the messages
      case message =>
        router.route(message, sender())
    }
  }

  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("RoutersDemo", ConfigFactory.load().getConfig("routersDemo"))
  val master = system.actorOf(Props[Master])

//  for (i <- 1 to 10) {
//    master ! s"[$i] Hello from the world"
//  }

  /**
    * Method #2 - a router actor with its own children
    * POOL router
    */
    // 2.1 programmatically (in code)
  val poolMaster = system.actorOf(RoundRobinPool(5).props(Props[Slave]), "simplePoolMaster")
//  for (i <- 1 to 10) {
//    poolMaster ! s"[$i] Hello from the world"
//  }

    // 2.2 from configuration
  val poolMaster2 = system.actorOf(FromConfig.props(Props[Slave]), "poolMaster2")
//  for (i <- 1 to 10) {
//    poolMaster2 ! s"[$i] Hello from the world"
//  }

  /**
    * Method #3 - router with actors created elsewhere
    * GROUP router
    */
  // .. in another part of my application
  val slaveList = (1 to 5).map(i => system.actorOf(Props[Slave], s"slave_$i")).toList

  // need their paths
  val slavePaths = slaveList.map(slaveRef => slaveRef.path.toString)

  // 3.1 in the code
  val groupMaster = system.actorOf(RoundRobinGroup(slavePaths).props())
//  for (i <- 1 to 10) {
//    groupMaster ! s"[$i] Hello from the world"
//  }

  // 3.2 from configuration
  val groupMaster2 = system.actorOf(FromConfig.props(), "groupMaster2")
  for (i <- 1 to 10) {
    groupMaster2 ! s"[$i] Hello from the world"
  }

  /**
    * Special messages
    */
  groupMaster2 ! Broadcast("hello, everyone")

  // PoisonPill and Kill are NOT routed
  // AddRoutee, Remove, Get handled only by the routing actor
}
