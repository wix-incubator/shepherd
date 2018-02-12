package com.wix.shepherd

import com.wix.shepherd.json.JsonSerdes.{String2jsonNode, toJson}
import com.wix.shepherd.messaging.{ClientsUpdatePublisher, RegistrationStore}
import com.wix.shepherd.sections.{OverviewSection, Section, Section2}
import com.wix.shepherd.types.SectionId
import com.wix.shepherd.websocket.SocketServer
import com.wix.shepherd.zookeeper.ZkSerdes
import kafka.utils.ZkUtils
import org.I0Itec.zkclient.{ZkClient, ZkConnection}
import org.springframework.boot._
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
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

  @Bean def zkUtils(config: ShepherdConfig) = {
    val notSecure = false
    new ZkUtils(new ZkClient(config.zookeeper, 5000, 5000, ZkSerdes), new ZkConnection(config.zookeeper), notSecure)
  }

  @Bean
  def sections(updater: ClientsUpdatePublisher, zkUtils: ZkUtils): Map[SectionId, Section] = Map(
    OverviewSection.sectionId -> new OverviewSection(updater, zkUtils),
    Section2.sectionId -> new Section2(updater))

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

case class ShepherdConfig(socketServerPort: Int, zookeeper: String, kafkaBrokers: String)

object ShepherdConfig {
  val default = ShepherdConfig(9902, "NO_ZOOKEEPER_SET", "NO_KAFKA_BROKERS_SET")
}