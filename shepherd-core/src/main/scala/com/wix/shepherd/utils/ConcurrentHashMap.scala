package com.wix.shepherd.utils

import java.util.concurrent
import java.util.{concurrent => juc}
import scala.collection.convert.Wrappers.JConcurrentMapWrapper

class ConcurrentHashMap[A, B] extends JConcurrentMapWrapper[A, B](new juc.ConcurrentHashMap[A, B]) {

  def doPutIfAbsent(a: A, doPut: => B) {
    if (!contains(a))
      underlying.synchronized {
        if (!contains(a)) {
          put(a, doPut)
        }
      }
  }

  def getOrPut(a: A, doPut: => B): B = {
    if (!contains(a))
      underlying.synchronized {
        if (!contains(a)) {
          val b: B = doPut
          put(a, b)
          b
        }
        else
          get(a).get
      }
    else
      get(a).get
  }
}

object ConcurrentHashMap {

  def apply[A, B](): ConcurrentHashMap[A, B] = new ConcurrentHashMap[A, B]

}