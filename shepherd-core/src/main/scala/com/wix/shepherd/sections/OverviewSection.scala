package com.wix.shepherd.sections

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.wix.shepherd.ShepherdServerUpdate
import com.wix.shepherd.messaging.{BrowserAddress, ClientsUpdatePublisher}

class OverviewSection(updater: ClientsUpdatePublisher) extends Section(OverviewSection.sectionId, updater) {
  def resetSubscriptions(subscriptions: Map[SectionSpecificSubscription, Set[BrowserAddress]], newSubscription: Option[BrowserSectionSubscription]) = {
    newSubscription.foreach {subscription =>
      updater.publishToClients(OverviewUpdate(Seq(BrokerInfo("kfk1.42.wixprod.net", "10Mbps")), subscription.browserAddress))
    }
  }
}

object OverviewSection {
  val sectionId = "overview"
}

@JsonIgnoreProperties(ignoreUnknown = true, value = Array("forBrowser"))
case class OverviewUpdate(brokers: Seq[BrokerInfo], forBrowser: BrowserAddress) extends ShepherdServerUpdate {
  override val specificBrowser: Option[BrowserAddress] = Some(forBrowser)
}

case class BrokerInfo(hostname: String, throughput: String)