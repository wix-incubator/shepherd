package com.wix.shepherd

import com.wix.shepherd.ShepherdTestEnv.webSocketDriver
import com.wix.shepherd.drivers.ShepherdDriver
import com.wix.shepherd.types._
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.BeforeAfterAll
import com.wix.e2e.http.matchers.ResponseMatchers._


class ShepherdServerIT extends SpecificationWithJUnit with BeforeAfterAll {

  "ShepherdServer" should {
    "communicate via http and web sockets" in {
      webSocketDriver.getMain must beSuccessful
      webSocketDriver.sendMessageToWebSocket(FetchOverview)

      eventually {
        webSocketDriver.lastReceivedWebSocketMessage must beSome(BrokerInfo("brokerId1", "address1"))
      }
    }
  }

  override def beforeAll() = {
    ShepherdTestEnv.start()
  }

  override def afterAll() = {}
}

object ShepherdTestEnv {
  val webSocketDriver = new ShepherdDriver(httpPort = 9901, webSocketPort = 9902)

  def start() = {
    ShepherdServer.start()
    webSocketDriver.startWebSocket()
  }
}

