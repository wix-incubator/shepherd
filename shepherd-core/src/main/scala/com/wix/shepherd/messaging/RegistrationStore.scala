package com.wix.shepherd.messaging

import java.util.concurrent.atomic.AtomicReference

import com.wix.shepherd.ShepherdServerUpdate
import com.wix.shepherd.sections.SectionSpecificSubscription
import com.wix.shepherd.utils.JavaUnaryOperatorConverter._
import com.wix.shepherd.websocket.BrowserSocket

class RegistrationStore extends ClientsUpdatePublisher {
  private val registrations = new AtomicReference[BrowserMappings](BrowserMappings.empty)

  def subscribe(browserAddress: BrowserAddress, sectionSubscription: SectionSpecificSubscription, webSocket: BrowserSocket) = {
    val mappings = registrations.updateAndGet { mappings: BrowserMappings =>
      val browser = mappings.browsers.getOrElse(browserAddress, Browser(webSocket))
      mappings.withSubscription(browserAddress, browser, sectionSubscription)
    }

    startPollingToBrowser(browserAddress, mappings)
  }

  def unsubscribe(browserAddress: BrowserAddress) = {
    val mappingsBeforeRemoval = registrations.getAndUpdate {
      mappings: BrowserMappings => mappings.withoutSubscription(browserAddress)
    }

    stopPollingToBrowser(browserAddress, mappingsBeforeRemoval)
  }

  def publishToClients(update: ShepherdServerUpdate, forBrowser: Option[BrowserAddress]) =
    forBrowser match {
      case None => currentBrowsers.broadcast(update)
      case Some(address) => currentBrowsers.browser(address).foreach(_.send(update))
    }

  private def currentBrowsers = registrations.get

  private def startPollingToBrowser(browserAddress: BrowserAddress, mappings: BrowserMappings) =
    mappings.browser(browserAddress).foreach(_.startPolling())

  private def stopPollingToBrowser(browserAddress: BrowserAddress, mappingsBeforeRemoval: BrowserMappings) =
    mappingsBeforeRemoval.browser(browserAddress).foreach(_.stop())
}

case class BrowserMappings private(browsers: Map[BrowserAddress, Browser], subscriptions: Map[BrowserAddress, SectionSpecificSubscription]) {
  def withSubscription(browserAddress: BrowserAddress, browser: Browser, subscription: SectionSpecificSubscription) = {
    copy(
      browsers = browsers + (browserAddress -> browser),
      subscriptions = subscriptions + (browserAddress -> subscription))
  }

  def withoutSubscription(browserAddress: BrowserAddress) = {
    copy(
      browsers = browsers - browserAddress,
      subscriptions = subscriptions - browserAddress
    )
  }

  def browser(browserAddress: BrowserAddress) = browsers.get(browserAddress)

  def broadcast(update: ShepherdServerUpdate) = browsers.values.foreach(_.send(update)) //todo: if send returns false (queue is full we should signal this to the ui)
}

object BrowserMappings {
  val empty = BrowserMappings(Map.empty, Map.empty)
}


trait ClientsUpdatePublisher {
  def publishToClients(update: ShepherdServerUpdate, specificClient: Option[BrowserAddress]): Unit
}