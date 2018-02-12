package com.wix.shepherd.sections

import com.wix.shepherd.ShepherdServerUpdate
import com.wix.shepherd.messaging.{BrowserAddress, ClientsUpdatePublisher}

class OverviewSection(updater: ClientsUpdatePublisher) extends Section(OverviewSection.sectionId, updater) {
  def resetSubscriptions(subscriptions: Map[SectionSpecificSubscription, Set[BrowserAddress]], newSubscription: Option[BrowserSectionSubscription]) = {
    newSubscription.foreach { subscription =>
      updater.publishToClients(OverviewUpdate(Seq(BrokerInfo("kfk1.42.wixprod.net", 0))), Some(subscription.browserAddress))
    }
  }
}

object OverviewSection {
  val sectionId = "overview"
}

case class OverviewUpdate(brokers: Seq[BrokerInfo]) extends ShepherdServerUpdate

case class BrokerInfo(hostname: String, partitions: Int)