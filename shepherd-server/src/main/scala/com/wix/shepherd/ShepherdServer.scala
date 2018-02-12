package com.wix.shepherd

import com.wix.shepherd.json.JsonSerdes.toJson
import com.wix.shepherd.json.JsonSerdes.String2jsonNode
import com.wix.shepherd.messaging.RegistrationStore
import com.wix.shepherd.sections.{OverviewSection, Section, Section2}
import com.wix.shepherd.types.SectionId
import com.wix.shepherd.websocket.SocketServer
import org.springframework.boot._
import org.springframework.boot.autoconfigure._
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment

import scala.util.Try

@EnableAutoConfiguration
class ShepherdServer {

  @Bean def config(env: Environment): ShepherdConfig =
    Try(env.getRequiredProperty("config").as[ShepherdConfig]).getOrElse(ShepherdConfig.default)

  @Bean def store = new RegistrationStore

  @Bean
  def socketServer(config: ShepherdConfig, store: RegistrationStore, sections: Map[SectionId, Section]) =
    new SocketServer(config.socketServerPort, store, sections)

  @Bean
  def sections(store: RegistrationStore): Map[SectionId, Section] = Map(
    OverviewSection.sectionId -> new OverviewSection(store),
    Section2.sectionId -> new Section2(store))

  @Bean
  def startSocketServer(socketServer: SocketServer) = {
    socketServer.start()
    socketServer
  }
}

object ShepherdServer {
  def main(args: Array[String]): Unit = {
    println(s"starting ShepherdServer with ${args.mkString(",")}")
    SpringApplication.run(classOf[ShepherdServer], args(0))
  }

  def start(config: ShepherdConfig): Unit = main(Array(s"--config=${toJson(config)}"))
}

case class ShepherdConfig(socketServerPort: Int)

object ShepherdConfig {
  val default = ShepherdConfig(9902)
}