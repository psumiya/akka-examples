package com.example

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.example.Greeter.Greet
import org.scalatest.wordspec.AnyWordSpecLike

class QuickstartSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "A Greeter" must {
    "listen to greeted" in {
      // Launch the actor under test
      val underTest = spawn(Greeter())
      // Send a test message to the actor under test
      underTest ! Greet("Hello Greeter!")
    }
  }

}
