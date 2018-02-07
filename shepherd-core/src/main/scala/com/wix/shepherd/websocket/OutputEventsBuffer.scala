package com.wix.shepherd.websocket

trait OutputEventsBuffer {
  def append[T <: AnyRef](event: T): Unit

}

