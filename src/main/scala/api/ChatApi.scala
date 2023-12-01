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
      concat(pathEnd {
        post {
          entity(as[StartChat]) { start =>
            val senderId = start.sender.id.toString
            val receiverId = start.receiver.id.toString
            logger.info(s"Starting new chat sender: $senderId, receiver: $receiverId")
            val eventualCreated =
              store
                .ask(ref => AddNewChat(start.sender, start.receiver, ref))
                .map(id => {
                  val chatLinks = service.generateChatLinks(id, senderId, receiverId)
                  ChatCreated(id, chatLinks._1, chatLinks._2)
                })
            onSuccess(eventualCreated) { c =>
              complete(StatusCodes.Created, c)
            }
          }
        }
      }, path(IntNumber / "messages" / Segment) { (id, userId) =>
        onSuccess(store.ask(ref => GetChatMeta(id, userId, ref))) {
          case Some(meta) => handleWebSocketMessages(websocketFlow(meta.userName, meta.ref))
          case None => complete(StatusCodes.NotFound)
        }
      })
    }
  }

  private def websocketFlow(userName: String, chatActor: ActorRef[ChatCommand]): Flow[Message, Message, Any] = {
    val source: Source[TextMessage, Unit] =
      ActorSource.actorRef[String](PartialFunction.empty, PartialFunction.empty, 5, OverflowStrategy.fail)
        .map[TextMessage](TextMessage(_))
        .mapMaterializedValue(sourceRef => chatActor ! AddNewUser(sourceRef))

    val sink: Sink[Message, Future[Done]] = Sink
      .foreach[Message] {
        case tm: TextMessage =>
          chatActor ! ProcessMessage(userName, tm.getStrictText)
        case _ =>
          logger.warn(s"User with id: '{}', send unsupported message", userName)
      }

    Flow.fromSinkAndSource(sink, source)
  }
}

object ChatApi {

  case class StartChat(sender: User, receiver: User)

  case class User(id: UUID, name: String)

  case class ChatCreated(chatId: Int, senderChatLink: String, receiverChatLink: String)

  implicit val startChatDecoder: Decoder[StartChat] = deriveDecoder
  implicit val startChatEncoder: Encoder[StartChat] = deriveEncoder
  implicit val userDecoder: Decoder[User] = deriveDecoder
  implicit val userEncoder: Encoder[User] = deriveEncoder
  implicit val chatCreatedDecoder: Decoder[ChatCreated] = deriveDecoder
  implicit val chatCreatedEncoder: Encoder[ChatCreated] = deriveEncoder
}
