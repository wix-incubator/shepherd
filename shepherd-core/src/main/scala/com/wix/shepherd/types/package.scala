package com.wix.shepherd

import com.fasterxml.jackson.annotation.JsonTypeInfo

package object types {

  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
  trait ShepherdEvent

  case class SectionMessage[T <: AnyRef](sectionId: SectionId,  event: T) extends ShepherdEvent

  case object FetchOverview extends ShepherdEvent

  case class BrokerInfo(brokerId: String, address: String) extends ShepherdEvent

  type SectionId = String
}
