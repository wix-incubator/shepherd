package com.wix.shepherd.utils

import java.util.Properties

case class ScalaProperties(properties: Map[String, String]) {
  val asJava = {
    val javaProperties = new Properties
    properties.foreach { property =>
      javaProperties.setProperty(property._1, property._2)
    }

    javaProperties
  }
}

object ScalaProperties {
  def propertiesFor(properties: (String, String)*) = ScalaProperties(properties.toMap)
  def javaProperties(properties: (String, String)*) = ScalaProperties(properties.toMap).asJava
  val empty = ScalaProperties(Map.empty)
}