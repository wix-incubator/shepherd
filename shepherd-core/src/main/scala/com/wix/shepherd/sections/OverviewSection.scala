package com.wix.shepherd.sections

import com.wix.shepherd.ShepherdServerUpdate
import com.wix.shepherd.messaging.{BrowserAddress, ClientsUpdatePublisher}
import kafka.cluster.EndPoint
import kafka.utils.ZkUtils

class OverviewSection(updater: ClientsUpdatePublisher, zkUtils: ZkUtils) extends Section(OverviewSection.sectionId, updater) {
  def resetSubscriptions(subscriptions: Map[SectionSpecificSubscription, Set[BrowserAddress]], newSubscription: Option[BrowserSectionSubscription]) = {
    newSubscription.foreach { subscription =>
      val brokerInfos = brokers.map(broker => BrokerInfo(broker.endPoints.map(toPrettyHostname).head, 0))
      updater.publishToClients(OverviewUpdate(brokerInfos.sortBy(_.hostname)), Some(subscription.browserAddress))
    }
  }

  private def brokers = zkUtils.getAllBrokersInCluster()

  private def toPrettyHostname(endpoint: EndPoint) = s"${endpoint.host}:${endpoint.port}"
}

object OverviewSection {
  val sectionId = "overview"
}

case class OverviewUpdate(brokers: Seq[BrokerInfo]) extends ShepherdServerUpdate

case class BrokerInfo(hostname: String, partitions: Int)