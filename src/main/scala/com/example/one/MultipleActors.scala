package com.example.one

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

object Greeter {

  final case class Greet(whom: String, replyTo: ActorRef[Greeted])

  final case class Greeted(whom: String, from: ActorRef[Greet])

  def apply(): Behavior[Greet] = Behaviors.receive { (context, message) =>
    context.log.info("Received Message {} in Greeter Actor.", message.whom)
    message.replyTo ! Greeted(message.whom, context.self)
    Behaviors.same
  }

}


object GreeterBot {

  def apply(max: Int = 2): Behavior[Greeter.Greeted] = {
    define(0, max)
  }

  def define(greetingCounter: Int, max: Int): Behavior[Greeter.Greeted] =
    Behaviors.receive { (context, message) =>
      val n = greetingCounter + 1
      context.log.info("Received Message {} for {} in Greeter Bot Actor.", n, message.whom)
      if (n == max) {
        Behaviors.stopped
      } else {
        message.from ! Greeter.Greet(message.whom, context.self)
        define(n, max)
      }
  }

}

object GreeterBots {

  final case class Launch(systemName: String)

  def apply(): Behavior[Launch] =
    Behaviors.setup { context =>

      val greeter = context.spawn(Greeter(), "Greeter")
      Behaviors.receiveMessage { message =>
        val replyTo = context.spawn(GreeterBot(), message.systemName)
        greeter ! Greeter.Greet(message.systemName, replyTo)
        Behaviors.stopped
      }
  }

}

// This is a quickstart showing multiple actors.
object MultipleActors extends App {

  val actorSystem = ActorSystem(GreeterBots(), "GreeterBots")
  actorSystem ! GreeterBots.Launch("Alpha")

}

