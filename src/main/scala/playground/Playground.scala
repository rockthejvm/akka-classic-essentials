package playground

import akka.actor.ActorSystem

import scala.annotation.tailrec

object Playground extends App {

  def anotherFactorial(x:Int):BigInt={

    @tailrec
    def helperFact(n:Int,accu:BigInt):BigInt=



      if(n <= 1)

        accu

      else

        helperFact(n-1, n * accu)



    helperFact(x, 1)





  }

  println(anotherFactorial(5000))
}