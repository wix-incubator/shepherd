package com.wix.shepherd.websocket

import java.net.InetSocketAddress
import java.util.concurrent.CountDownLatch

import com.wix.shepherd.{RegistrationEvent, ShepherdClientRequest}
import com.wix.shepherd.json.JsonSerdes._
import com.wix.shepherd.messaging.{BrowserAddress, RegistrationStore}
import com.wix.shepherd.sections.{BrowserSectionSubscription, Section}
import com.wix.shepherd.types._
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer

class SocketServer(port: Int, store: RegistrationStore, sections: Map[SectionId, Section]) {
  private val startupLatch = new CountDownLatch(1)

  sys.addShutdownHook(server.stop())

  private val server = new WebSocketServer(new InetSocketAddress(port)) {
    override def onMessage(webSocket: WebSocket, msg: String): Unit = {
      println(msg)
      msg.as[ShepherdClientRequest] match {
        case reg: RegistrationEvent => registerBrowser(webSocket, reg)
        case other => println(other)
      }
    }

    override def onError(webSocket: WebSocket, e: Exception): Unit = {
      println("onError")
      e.printStackTrace()
    }

    override def onClose(webSocket: WebSocket, i: Int, s: String, b: Boolean): Unit = {
      store.unsubscribe(BrowserAddress.from(webSocket))

    }

    override def onOpen(webSocket: WebSocket, clientHandshake: ClientHandshake): Unit = {
      println("onOpen")
    }

    override def onStart() = {
      println("counting down latch")
      startupLatch.countDown()
    }
  }

  private def registerBrowser(webSocket: WebSocket, registration: RegistrationEvent) = {
    val browserAddress = BrowserAddress.from(webSocket)
    store.subscribe(browserAddress, registration.sectionSpecificSubscription, new JsonSerializingWebSocket(webSocket))

    subscribeOnSection(registration, browserAddress)
  }

  private def subscribeOnSection(registration: RegistrationEvent, browserAddress: BrowserAddress) =
    sections.get(registration.sectionId).foreach(_.subscribe(BrowserSectionSubscription(browserAddress, registration.sectionSpecificSubscription)))

  private def unsubscribeOnSection(browserAddress: BrowserAddress) =
    sections.values.foreach(_.unsubscribe(browserAddress))

  def start() = {
    server.start()
    println("waiting for latch...")
    startupLatch.await()
    println("done")
  }

  def stop() = server.stop()
}