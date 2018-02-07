package com.wix.shepherd.sections

import com.wix.shepherd.messaging.{BrowserAddress, ClientsUpdatePublisher}
import com.wix.shepherd.types.SectionId

class Section2(updater: ClientsUpdatePublisher) extends Section(Section2.sectionId, updater) {
  override def resetSubscriptions(subscriptions: Map[SectionSpecificSubscription, Set[BrowserAddress]], newSubscription: Option[BrowserSectionSubscription]) = {}
}

object Section2 {
  val sectionId = "section2"
}

case class Section2Subscription(i: Int) extends SectionSpecificSubscription {
  val sectionId: SectionId = "2"
}