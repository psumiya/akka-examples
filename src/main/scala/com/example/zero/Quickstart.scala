package com.example.zero

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}

/**
 * To create an actor, specify a behavior when it receives a message.
 */
object Greeter {

  // Message Types this actor can receive. Typically declared as case classes.
  final case class Greet(greeting: String)

  /**
   * Behavior definition on receipt of a message.
   * <p>
   * Actors are reactive and message driven.
   * </p><p>
   * They are suspended silently in memory until a message is received in their mailbox.
   * </p>
   *
   * @return a behavior for a greeting
   */
  def apply(): Behavior[Greet] = Behaviors.receive { (context, message) =>
    context.log.info("Received message: {}!", message.greeting)
    Behaviors.same
  }

}

/**
 * This is a quickstart showing a single actor.
 * <p>
 * The example will initialize an ActorSystem, this is the entry point into Akka.
 * </p><p>
 * The actorSystem takes a name and a guardian actor, the guardian typically bootstraps the application.
 * </p><p>
 * The example then sends a message asynchronously using the `!` (tell) method.
 * This puts a message into the actor's mailbox, which is a message queue with some defined ordering.
 * </p><p>
 * Multiple senders can send messages to an actor's mailbox, and the order of messages from the same sender is preserved.
 * The communication between sender and receiver is asynchronous, meaning the sender does not wait for a response
 * or acknowledgement and continues with other work.
 * </p>
 */
object Quickstart extends App {

  val actorSystem = ActorSystem(Greeter(), "Greeter")
  actorSystem ! Greeter.Greet("Hello!")

}
