package com.wix.shepherd

import com.wix.KafkaCluster
import com.wix.shepherd.ShepherdTestEnv.shepherdDriver
import com.wix.shepherd.drivers.ShepherdDriver
import com.wix.shepherd.sections.{BrokerInfo, OverviewUpdate}
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.BeforeAfterAll

import scala.util.Random

class ShepherdServerIT extends SpecificationWithJUnit with BeforeAfterAll {

  "ShepherdServer" should {
    "when stepping into overview section - receive a list of the kafka servers" in {
      shepherdDriver.registerToOverview()

      eventually {
        lastUpdate must beSome(OverviewUpdate(Seq(
          BrokerInfo("127.0.0.1:10001", 0), BrokerInfo("127.0.0.1:10002", 0), BrokerInfo("127.0.0.1:10003", 0))))
      }
    }
  }

  private def lastUpdate = shepherdDriver.lastUpdate

  override def beforeAll() = {
    ShepherdTestEnv.start()
  }

  override def afterAll() = {
    ShepherdTestEnv.stop()
  }
}

object ShepherdTestEnv {
  val socketServerPort = Random.nextInt(50000) + 2000
  val shepherdDriver = new ShepherdDriver(httpPort = 9901, webSocketPort = socketServerPort)
  val kafkaCluster = new KafkaCluster

  private lazy val startOnceLazy = {
    kafkaCluster.start(3)
    ShepherdServer.start(ShepherdConfig(socketServerPort, kafkaCluster.zookeeperAddress, kafkaCluster.brokersAddress))
    shepherdDriver.startWebSocket()
  }

  def start() = startOnceLazy

  def stop() = {
    kafkaCluster.stop()
    shepherdDriver.shutdown()
  }
}

