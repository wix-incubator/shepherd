package com.wix.shepherd.drivers

import java.net.URI
import java.util.concurrent.atomic.AtomicReference

import com.wix.shepherd.json.JsonSerdes._
import com.wix.shepherd.types.ShepherdEvent
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake

class WebSocketDriver(port: Int) {
  val lastClientMessageRef = new AtomicReference[Option[String]]()

  val client = new WebSocketClient(new URI(s"ws://localhost:$port")) {
    override def onError(e: Exception): Unit = throw e

    override def onMessage(s: String) = lastClientMessageRef.set(Option(s))

    override def onClose(i: Int, s: String, b: Boolean): Unit = println("[client] closed")

    override def onOpen(serverHandshake: ServerHandshake) = println("[client] open")
  }

  def connect(): Unit = client.connectBlocking()

  def send[T <: AnyRef](msg: T) = client.getConnection.send(msg.asJsonStr)

  def lastClientMessage[T <: AnyRef] = lastClientMessageRef.get().map(_.as[ShepherdEvent])
}
