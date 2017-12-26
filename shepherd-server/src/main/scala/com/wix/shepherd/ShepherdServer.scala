package com.wix.shepherd

import javax.annotation.PostConstruct

import com.wix.shepherd.websocket.SocketServer
import org.springframework.boot._
import org.springframework.boot.autoconfigure._
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation._

@EnableAutoConfiguration
class ShepherdServer {
  @Bean def controller = new SomeController
}

object ShepherdServer {
  def main(args: Array[String]): Unit = SpringApplication.run(classOf[ShepherdServer])

  def start(): Unit = main(Array.empty)
}

@RestController
class SomeController {
  @RequestMapping(Array("/topics"))
  def topics = "topics"

  def socketServer = new SocketServer(9902)

  @PostConstruct
  def startSocketServer() = {
    socketServer.start()
  }

}
