package com.example

import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

/**
 * To create an actor, simple specify a behavior when it receives a message.
 */
object Greeter {

  // Message(s) this actor can receive. Typically declared as case classes.
  final case class Greet(request: String)

  // Behavior definition on receipt of above message
  def apply(): Behavior[Greet] = Behaviors.receive { (context, message) =>
    context.log.info("Received message: {}!", message.request)
    Behaviors.same
  }

}

object Quickstart extends App {

  // Initialize an actor system
  val actorSystem = ActorSystem(Greeter(), "Greeter")
  // Send a message
  actorSystem ! Greeter.Greet("Hello!")

}
