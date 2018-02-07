package com.wix.shepherd

import com.wix.shepherd.messaging.RegistrationStore
import com.wix.shepherd.sections.{OverviewSection, Section, Section2}
import com.wix.shepherd.types.SectionId
import com.wix.shepherd.websocket.SocketServer
import org.springframework.boot._
import org.springframework.boot.autoconfigure._
import org.springframework.context.annotation.Bean

@EnableAutoConfiguration
class ShepherdServer {
  @Bean def store = new RegistrationStore

  @Bean
  def socketServer(store: RegistrationStore, sections: Map[SectionId, Section]) =
    new SocketServer(9902, store, sections)

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
  def main(args: Array[String]): Unit = SpringApplication.run(classOf[ShepherdServer])

  def start(): Unit = main(Array.empty)
}