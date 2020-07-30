package com.example.one

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

/**
 * The Greeter actor can understand two commands (messages), Greet, and Greeted.
 * <ol>
 *   <li>MultipleActors bootstraps the processing system</li>
 *   <li>GreeterBots creates a Greeter actor, and a GreeterBot actor</li>
 *   <li>GreeterBots triggers a greet message to the greeter actor, while setting the sender of that message as the GreeterBot actor</li>
 *   <li>When Greeter actor replies, the reply is received by the GreeterBot actor. The behavior of this actor is set
 *   to keep sending a new message to the Greeter actor until a configured max is hit</li>
 * </ol>
 * The max counter is a simple incrementer without any explicit thread-safety magic around it.
 * All messages are being exchanged in a multi-threaded way, without requiring us to explicitly use any thread-safe objects or concurrency best practices.
 */
object Greeter {

  /**
   * Message type to command the actor to greet someone
   *
   * @param whom whom to greet
   * @param replyTo the address of the sender who sent this message
   */
  final case class Greet(whom: String, replyTo: ActorRef[Greeted])

  /**
   * Message type for the Greeter to confirm it has Greeted someone
   *
   * @param whom whom has it greeted
   * @param from the address of the greeter
   */
  final case class Greeted(whom: String, from: ActorRef[Greet])

  /**
   * The behavior of an actor is defined with a `receive` behavior factory.
   * Processing a message can result in a new behavior different than this one, however
   * this example is not resulting in a new behavior and hence returns Behaviors.same.
   *
   * @return the greeter behavior
   */
  def apply(): Behavior[Greet] = Behaviors.receive { (context, message) =>
    context.log.info("Hello {}!", message.whom)
    context.log.info("2. Send a reply for message {} from {} to {}", message.whom, context.self, message.replyTo)
    message.replyTo ! Greeted(message.whom, context.self)
    Behaviors.same
  }

}

/**
 * The bot actor that sends messages to the greeter actors
 */
object GreeterBot {

  /**
   *
   * @param max the max number of message a bot will send
   * @return the bot behavior
   */
  def apply(max: Int = 2): Behavior[Greeter.Greeted] = {
    define(0, max)
  }

  def define(greetingCounter: Int, max: Int): Behavior[Greeter.Greeted] =
    Behaviors.receive { (context, message) =>
      val n = greetingCounter + 1
      context.log.info("Received Greeting {} for {}", n, message.whom)
      if (n == max) {
        Behaviors.stopped
      } else {
        context.log.info("3. Send a message {} from {} to {}", message.whom, context.self, message.from)
        message.from ! Greeter.Greet(message.whom, context.self)
        define(n, max)
      }
  }

}

/**
 * Sets up the bots and the greeter actors
 */
object GreeterBots {

  final case class Launch(systemName: String)

  def apply(): Behavior[Launch] =
    Behaviors.setup { context =>
      // Create a greeter actor
      val greeter = context.spawn(Greeter(), "Greeter")
      Behaviors.receiveMessage { message =>
        // Create the bot actors
        val replyTo = context.spawn(GreeterBot(), message.systemName)
        context.log.info("1. Send a message {} from {} to {} when in {}", message.systemName, replyTo, greeter, context.self)
        greeter ! Greeter.Greet(message.systemName, replyTo)
        Behaviors.stopped
      }
  }

}

/**
 * This is a quickstart showing multiple actors.
 * <p>
 * The example will initialize an ActorSystem, this is the entry point into Akka.
 * </p>
 */
object MultipleActors extends App {

  val actorSystem = ActorSystem(GreeterBots(), "GreeterBots")
  actorSystem ! GreeterBots.Launch("Charlie")

}

