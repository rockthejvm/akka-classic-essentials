package playground

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

/**
  * A simple Scala application. I recommend you fiddle with the code and try your own here.
  * Have fun!
  *
  * Daniel for Rock the JVM
  */
object Playground extends App {

  val system = ActorSystem("Playground")

  val playgroundActor = system.actorOf(Props[MyPlaygroundActor], "playgroundActor")
  playgroundActor ! "I love Akka!"

  // your code here
}

class MyPlaygroundActor extends Actor with ActorLogging {
  override def receive = {
    case m => log.info(m.toString)
  }
}