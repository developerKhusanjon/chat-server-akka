package dev.khusanjon
package actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import dev.khusanjon.actors.Chat.ChatCommand

import scala.collection.mutable

object ChatRoom {

  sealed trait RoomCommand

  final case class AddNewChat(sender: User, receiver: User, replyTo: ActorRef[Int]) extends RoomCommand

  final case class GetChatMeta(chatId: Int, userName: String, replyTo: ActorRef[Option[GetChatMetaResponse]]) extends RoomCommand

  final case class GetChatMetaResponse(userName: String, ref: ActorRef[ChatCommand])

  private var sequence = 0
  private val room = mutable.Map.empty[Int, ChatMetaData]

  private case class ChatMetaData(participants: Map[String, User], ref: ActorRef[ChatCommand]) {
    def containUserId(userId: String): Boolean =
      participants.contains(userId)
  }

  def apply(): Behavior[RoomCommand] =
    Behaviors.setup { context => {
      Behaviors.receiveMessage {
        case AddNewChat(sender, receiver, replyTo) =>
          sequence += 1
          val newChat: ActorRef[ChatCommand] = context.spawn(Chat(), s"Chat$sequence")
          val participants = Map(sender.id.toString -> sender, receiver.id.toString -> receiver)
          val metadata = ChatMetaData(participants, newChat)
          room.put(sequence, metadata)
          replyTo ! sequence
          Behaviors.same
        case GetChatMeta(chatId, userId, replyTo) =>
          val chatRef = room
            .get(chatId)
            .filter(_.containUserId(userId))
            .flatMap(meta =>
              meta.participants
                .get(userId)
                .map(user => GetChatMetaResponse(user.name, meta.ref))
            )
          replyTo ! chatRef
          Behaviors.same
      }
    }
    }

}
