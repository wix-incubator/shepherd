package com.wix.shepherd.websocket

import com.wix.shepherd.json.JsonSerdes._
import org.java_websocket.WebSocket

trait SectionSocket {
  def send[T <: AnyRef](s: T): Unit
}

class WebSectionSocket(webSocket: WebSocket) extends SectionSocket {
  override def send[T <: AnyRef](s: T) = webSocket.send(s.asJsonStr)
}