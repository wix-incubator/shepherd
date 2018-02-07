package com.wix.shepherd.drivers

import java.net.URI
import java.util.concurrent.atomic.AtomicReference

import com.wix.e2e.http.BaseUri
import com.wix.shepherd.{RegistrationEvent, ShepherdClientRequest, ShepherdServerUpdate}
import com.wix.shepherd.json.JsonSerdes._
import com.wix.shepherd.sections.OverviewSection
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake

class ShepherdDriver(httpPort: Int, webSocketPort: Int) {
  implicit val baseUri = BaseUri(port = httpPort)

  private val webSocketClient = new ShepherdWebSocketClient(webSocketPort)

  def startWebSocket(): Unit = webSocketClient.connectBlocking()

  def registerToOverview() = sendMessageToWebSocket(RegistrationEvent(OverviewSection.sectionId))

  def lastUpdate[T <: ShepherdServerUpdate] = webSocketClient.lastClientMessageRef.get().map(_.as[ShepherdServerUpdate])

  private def sendMessageToWebSocket[T <: ShepherdClientRequest](msg: T) = webSocketClient.getConnection.send(msg.asJsonStr)
}

private class ShepherdWebSocketClient(port: Int) extends WebSocketClient(new URI(s"ws://localhost:$port")) {
  val lastClientMessageRef = new AtomicReference[Option[String]]()

  override def onError(e: Exception): Unit = throw e

  override def onMessage(s: String) = lastClientMessageRef.set(Option(s))

  override def onClose(i: Int, s: String, b: Boolean): Unit = println("[client] closed")

  override def onOpen(serverHandshake: ServerHandshake) = println("[client] open")
}