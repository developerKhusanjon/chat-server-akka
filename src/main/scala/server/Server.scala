package dev.khusanjon
package server

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import dev.khusanjon.config.HttpConfig
import org.slf4j.Logger

import scala.util.{Failure, Success}

object Server {

  def start(routes: Route, config: HttpConfig, logger: Logger)(implicit system: ActorSystem[_]) = {
    import system.executionContext

    val bindingFuture = Http()
      .newServerAt(config.host, config.port)
      .bind(routes)
    bindingFuture.onComplete {
      case Failure(exception) =>
        logger.error("Failed to bind HTTP endpoint, terminating system", exception)
        system.terminate()
      case Success(value) =>
        val address = value.localAddress
        logger.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
    }
  }

}
