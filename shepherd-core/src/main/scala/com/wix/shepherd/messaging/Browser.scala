package com.wix.shepherd.messaging

import java.util.concurrent.{ArrayBlockingQueue, Executors}
import java.util.concurrent.TimeUnit.MILLISECONDS

import com.wix.shepherd.ShepherdServerUpdate
import com.wix.shepherd.websocket.BrowserSocket

import scala.concurrent.{ExecutionContext, Future}

case class Browser(private val webSocket: BrowserSocket) {
  private implicit val ec = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  private val queue = new ArrayBlockingQueue[ShepherdServerUpdate](5000)
  @volatile private var running = false

  lazy private val startPollingOnce = {
    running = true
    while (running) {
      Option(queue.poll(10, MILLISECONDS)).foreach(webSocket.send)
    }
  }

  def send(event: ShepherdServerUpdate) = queue.offer(event)

  def startPolling() = Future(startPollingOnce)

  def stop() = running = false
}