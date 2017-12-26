package com.wix.shepherd

import com.wix.e2e.http.BaseUri
import com.wix.e2e.http.client.sync._
import com.wix.e2e.http.matchers.ResponseMatchers._
import com.wix.shepherd.ShepherdTestEnv.webSocketDriver
import com.wix.shepherd.drivers.WebSocketDriver
import com.wix.shepherd.types._
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.BeforeAfterAll

class ShepherdServerIT extends SpecificationWithJUnit with BeforeAfterAll {
  implicit val baseUri = BaseUri(port = 9901)

  "ShepherdServer" should {
    "be up" in {
      get("/topics") must beSuccessful
      webSocketDriver.send(FetchOverview)

      eventually {
        webSocketDriver.lastClientMessage must beSome(BrokerInfo("brokerId1", "address1"))
      }
    }
  }

  override def beforeAll() = {
    ShepherdTestEnv.start()
  }

  override def afterAll() = {}
}

object ShepherdTestEnv {
  val webSocketDriver = new WebSocketDriver(9902)

  def start() = {
    ShepherdServer.start()
    webSocketDriver.connect()
  }
}