package com.wix

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import java.util.function.UnaryOperator

import scala.concurrent.duration.DurationInt
import com.wix.KafkaCluster.BrokerId
import org.apache.curator.test.TestingServer

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

class KafkaCluster {
  private val brokersRef = new AtomicReference[Map[BrokerId, EmbeddedKafka]](Map.empty)
  private val zkPort = 2181
  private val zookeeper = new TestingServer(zkPort, false)
  private implicit val executor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  def start(brokerCount: Int) = {
    brokersRef.updateAndGet(new UnaryOperator[Map[BrokerId, EmbeddedKafka]] {
      override def apply(t: Map[BrokerId, EmbeddedKafka]) = {
        if (t.nonEmpty) throw ClusterAlreadyRunningException
        else startZookeeperAndBrokers(brokerCount)
      }
    })
  }

  private def startZookeeperAndBrokers(brokerCount: BrokerId) = {
    zookeeper.start()
    startBrokers(brokerCount)
  }

  private def startBrokers(brokers: BrokerId) = {
    val future = Future.sequence(Range.inclusive(1, brokers).map { brokerIndex =>
      val brokerId = 10000 + brokerIndex
      val embeddedKafka = new EmbeddedKafka(brokerId, zkPort)
      Future {
        embeddedKafka.start()
        brokerId -> embeddedKafka
      }
    })

    Await.result(future, 30.second).toMap
  }

  def stop() = {
    stopAllBrokers()
    tryStoppingZookeeper()
    clearBrokersReference()
  }

  private def tryStoppingZookeeper() = Try(zookeeper.stop())

  private def stopAllBrokers() =
    Await.ready(Future.sequence(brokersRef.get.map { case (_, kafka) => Future(kafka.stop()) }), 30.seconds)

  private def clearBrokersReference() = brokersRef.updateAndGet(new UnaryOperator[Map[BrokerId, EmbeddedKafka]] {
    override def apply(t: Map[BrokerId, EmbeddedKafka]) = Map.empty
  })
}

object KafkaCluster {
  type BrokerId = Int
  type BrokerAddress = String
}

case object ClusterAlreadyRunningException extends RuntimeException("Kafka cluster is already running, run stop() before start()ing.")