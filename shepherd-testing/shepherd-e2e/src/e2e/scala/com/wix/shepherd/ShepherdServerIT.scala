package com.wix.shepherd

import com.wix.shepherd.ShepherdTestEnv.shepherdDriver
import com.wix.shepherd.drivers.ShepherdDriver
import com.wix.shepherd.sections.{BrokerInfo, OverviewUpdate}
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.BeforeAfterAll

class ShepherdServerIT extends SpecificationWithJUnit with BeforeAfterAll {

  "ShepherdServer" should {
    "communicate via http and web sockets" in {
      shepherdDriver.registerToOverview()

      eventually {
        lastUpdate.get.asInstanceOf[OverviewUpdate].brokers === Seq(BrokerInfo("kfk1.42.wixprod.net", "10Mbps"))
      }
    }
  }

  private def lastUpdate = shepherdDriver.lastUpdate

  override def beforeAll() = {
    ShepherdTestEnv.start()
  }

  override def afterAll() = {}
}

object ShepherdTestEnv {
  val shepherdDriver = new ShepherdDriver(httpPort = 9901, webSocketPort = 9902)

  private lazy val startOnceLazy = {
    ShepherdServer.start()
    shepherdDriver.startWebSocket()
    val x = 5
  }

  def start() = startOnceLazy
}

