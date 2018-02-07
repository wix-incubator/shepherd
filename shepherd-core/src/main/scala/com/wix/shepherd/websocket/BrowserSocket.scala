package com.wix.shepherd.websocket

import com.wix.shepherd.ShepherdServerUpdate
import com.wix.shepherd.json.JsonSerdes._
import org.java_websocket.WebSocket

trait BrowserSocket {
  def send[T <: ShepherdServerUpdate](s: T): Unit
}

class JsonSerializingWebSocket(webSocket: WebSocket) extends BrowserSocket {
  override def send[T <: ShepherdServerUpdate](s: T): Unit = {
    webSocket.send(s.asJsonStr)
  }
}