package dev.khusanjon

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import dev.khusanjon.actors.ChatRoom
import dev.khusanjon.api.ChatApi
import dev.khusanjon.config.AppConfig
import dev.khusanjon.server.Server
import dev.khusanjon.services.ChatService
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object ChatAppStarter {

  def main(args: Array[String]): Unit = {
    val appConfig = ConfigSource.resources("chat-app.conf").loadOrThrow[AppConfig]
    val akkaConfig = ConfigSource.resources("akka.conf").config().right.get
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      context.setLoggerName("ChatApiLogger")
      val room = context.spawn(ChatRoom(), "Store")
      val service = new ChatService(List("api", "v1"), appConfig.httpConfig)
      val api = new ChatApi(service, room, context.log)(context.system)
      Server.start(api.routes, appConfig.httpConfig, context.log)(context.system)
      Behaviors.same
    }
    ActorSystem[Nothing](rootBehavior, "ChatApp", akkaConfig)
  }
}