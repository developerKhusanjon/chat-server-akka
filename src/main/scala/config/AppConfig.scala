package dev.khusanjon
package config

case class AppConfig(httpConfig: HttpConfig)
case class HttpConfig(port: Int, host: String) {
  val toPath = s"$host:$port"
}