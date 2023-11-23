package dev.khusanjon
package api

import services.ChatService

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.util.Timeout
import dev.khusanjon.actors.ChatRoom.RoomCommand
import org.slf4j.Logger

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt

class ChatApi(service: ChatService, room: ActorRef[RoomCommand], logger: Logger)(implicit val system: ActorSystem[_]) {

  private implicit val timeout: Timeout = Timeout(2.seconds)
  private implicit val ec: ExecutionContextExecutor = system.executionContext

  val routes: Route = {
    pathPrefix(service.path / "chats") {
      concat( pathEnd {

      }

      )
    }
  }
}
