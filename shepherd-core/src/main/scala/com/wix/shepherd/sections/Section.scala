package com.wix.shepherd.sections

import java.util.concurrent.atomic.AtomicReference

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.wix.shepherd.messaging.{BrowserAddress, ClientsUpdatePublisher}
import com.wix.shepherd.types.SectionId
import com.wix.shepherd.utils.JavaUnaryOperatorConverter._

abstract class Section(sectionId: SectionId, clientUpdater: ClientsUpdatePublisher) {
  private val subscriptionCounters = new AtomicReference[Map[SectionSpecificSubscription, Set[BrowserAddress]]](Map.empty)

  def subscribe(browserSubscription: BrowserSectionSubscription) = {
    val after = subscriptionCounters.updateAndGet { map: Map[SectionSpecificSubscription, Set[BrowserAddress]] =>
      map + (browserSubscription.sectionSpecificSubscription -> (map.getOrElse(browserSubscription.sectionSpecificSubscription, Set.empty) + browserSubscription.browserAddress))
    }

    reset(after, Some(browserSubscription))
  }

  def unsubscribe(browserAddress: BrowserAddress) = {
    val after = subscriptionCounters.updateAndGet { map: Map[SectionSpecificSubscription, Set[BrowserAddress]] =>
      map.find { case (s, b) => b.contains(browserAddress) }.map { case (subscription, browsers) =>
        map + (subscription -> (browsers - browserAddress))
      }.getOrElse(map)
    }

    reset(after)
  }

  private def reset(currentSubscriptions: Map[SectionSpecificSubscription, Set[BrowserAddress]], newSubscription: Option[BrowserSectionSubscription] = None) = {
    resetSubscriptions(currentSubscriptions.filter { case (_, browsers) => browsers.nonEmpty }, newSubscription)
  }

  def resetSubscriptions(subscriptions: Map[SectionSpecificSubscription, Set[BrowserAddress]], newSubscription: Option[BrowserSectionSubscription]): Unit
}

case class BrowserSectionSubscription(browserAddress: BrowserAddress, sectionSpecificSubscription: SectionSpecificSubscription)


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
trait SectionSpecificSubscription

object SectionSpecificSubscription {

  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
  object NoSpecificSectionSubscription extends SectionSpecificSubscription

}