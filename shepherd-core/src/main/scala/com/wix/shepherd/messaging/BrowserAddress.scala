package com.wix.shepherd.messaging

import org.java_websocket.WebSocket

case class BrowserAddress(host: String, port: Int)

object BrowserAddress {
  def from(webSocket: WebSocket) = {
    val address = webSocket.getRemoteSocketAddress
    BrowserAddress(address.getHostString, address.getPort)
  }
}
