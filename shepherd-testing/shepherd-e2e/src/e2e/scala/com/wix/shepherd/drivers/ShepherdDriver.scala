package com.wix.shepherd.drivers

import java.net.URI
import java.util.concurrent.atomic.AtomicReference

import com.wix.shepherd.json.JsonSerdes._
import com.wix.shepherd.types.ShepherdEvent
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake

import com.wix.e2e.http.BaseUri
import com.wix.e2e.http.client.sync._

class ShepherdDriver(httpPort: Int, webSocketPort: Int) {
  implicit val baseUri = BaseUri(port = httpPort)

  private val webSocketClient = new ShepherdWebSocketClient(webSocketPort)

  def startWebSocket(): Unit = webSocketClient.connectBlocking()

  def sendMessageToWebSocket[T <: AnyRef](msg: T) = webSocketClient.getConnection.send(msg.asJsonStr)

  def lastReceivedWebSocketMessage[T <: AnyRef] = webSocketClient.lastClientMessageRef.get().map(_.as[ShepherdEvent])

  def getMain = get("/topics")
}

private class ShepherdWebSocketClient(port: Int) extends WebSocketClient(new URI(s"ws://localhost:$port")) {
  val lastClientMessageRef = new AtomicReference[Option[String]]()

  override def onError(e: Exception): Unit = throw e

  override def onMessage(s: String) = lastClientMessageRef.set(Option(s))

  override def onClose(i: Int, s: String, b: Boolean): Unit = println("[client] closed")

  override def onOpen(serverHandshake: ServerHandshake) = println("[client] open")
}