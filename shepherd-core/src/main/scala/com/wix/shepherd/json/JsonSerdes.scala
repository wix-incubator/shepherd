package com.wix.shepherd.json

import java.lang.reflect.{ParameterizedType, Type}

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.{DeserializationFeature, JsonNode, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

object JsonSerdes {
  private val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  def asJsonStr(value: Map[Symbol, Any]): String =
    toJson(value map { case (k, v) => k.name -> v })

  def toJson(value: Any): String = mapper.writeValueAsString(value)

  implicit class AnyObject2JsonNode(o: AnyRef) {
    def asJson: JsonNode = asObjectNode

    def asObjectNode: ObjectNode = mapper.valueToTree(o)

    def asJsonStr: String = mapper.writeValueAsString(o)
  }

  implicit class String2jsonNode(value: String) {
    def as[T](implicit mn: Manifest[T]): T = {
      mapper.readValue(value, new TypeReference[T] {
        override def getType = typeFromManifest(manifest[T])
      })
    }

    private def typeFromManifest(m: Manifest[_]): Type = {
      if (m.typeArguments.isEmpty) {
        m.runtimeClass
      }
      else new ParameterizedType {
        def getRawType = m.runtimeClass

        def getActualTypeArguments = m.typeArguments.map(typeFromManifest).toArray

        def getOwnerType = null
      }
    }
  }

}

