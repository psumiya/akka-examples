package com.example.zero

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}

/**
 * To create an actor, specify a behavior when it receives a message.
 */
object Greeter {

  // Message Types this actor can receive. Typically declared as case classes.
  final case class Greet(greeting: String)

  // Behavior definition on receipt of above message
  // Actors are reactive and message driven
  // They are suspended silently in memory until a message is received in their mailbox
  def apply(): Behavior[Greet] = Behaviors.receive { (context, message) =>
    context.log.info("Received message: {}!", message.greeting)
    Behaviors.same
  }

}

object Quickstart extends App {

  // Initialize an ActorSystem, this is the entry point into Akka
  // It takes a name and a guardian actor, the guardian typically bootstraps the application
  val actorSystem = ActorSystem(Greeter(), "Greeter")

  // Send a message asynchronously using the `!` (tell) method
  // This puts a message into the actor's mailbox, which is a message queue with some ordering defined
  // Multiple senders can send messages to an actor's mailbox, the order of messages from the same sender is preserved
  // The communication between sender and receiver is asynchronous, meaning the sender does not wait for a response
  // or acknowledgement and continues with other work
  actorSystem ! Greeter.Greet("Hello!")

}
