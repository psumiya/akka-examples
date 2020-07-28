package com.example.one

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

/**
 * The Greeter actor can understand two commands (messages), Greet, and Greeted.
 * <ol>
 *   <li>MultipleActors bootstraps the processing system</li>
 *   <li>GreeterBots creates a Greeter actor, and a GreeterBot actor</li>
 *   <li>GreeterBots sends a greet message to the greeter actor</li>
 *   <li></li>
 * </ol>
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
    context.log.info("Received Message {} in Greeter Actor {}.", message.whom, context.self)
    context.log.info("Send a Greeted message to {} from {}", message.replyTo, context.self)
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
      context.log.info("Received Message {} for {} in Greeter Bot Actor {}.", n, message.whom, context.self)
      if (n == max) {
        Behaviors.stopped
      } else {
        context.log.info("Send a greet message {} to the greeter actor {}, with the replyTo address being the bot actor {}", message.whom, message.from, context.self)
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
        context.log.info("Send a greet message {} to the greeter actor {}, with the replyTo address being the bot actor {}", message.systemName, greeter, replyTo)
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
  actorSystem ! GreeterBots.Launch("Alpha")

}

