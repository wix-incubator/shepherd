package com.wix.shepherd.websocket

import java.net.InetSocketAddress
import java.util.concurrent.CountDownLatch

import com.wix.shepherd.json.JsonSerdes._
import com.wix.shepherd.types.{BrokerInfo, _}
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer

class SocketServer(port: Int) {
  private val startupLatch = new CountDownLatch(1)

  sys.addShutdownHook(server.stop())

  private val server = new WebSocketServer(new InetSocketAddress(port)) {
    override def onError(webSocket: WebSocket, e: Exception): Unit = {
      println("onError")
      e.printStackTrace()
    }

    override def onMessage(webSocket: WebSocket, msg: String): Unit = {
      val socket = new WebSectionSocket(webSocket)

      msg.as[ShepherdEvent] match {
        case FetchOverview => socket.send(BrokerInfo("brokerId1", "address1"))
        case _ =>
      }
    }

    override def onClose(webSocket: WebSocket, i: Int, s: String, b: Boolean): Unit = {
      println("onClose")
    }

    override def onOpen(webSocket: WebSocket, clientHandshake: ClientHandshake): Unit = {
      println("onOpen")
    }

    override def onStart() = {
      println("counting down latch")
      startupLatch.countDown()
    }
  }

  def start() = {
    server.start()
    println("waiting for latch...")
    startupLatch.await()
    println("done")
  }

  def stop() = server.stop()
}