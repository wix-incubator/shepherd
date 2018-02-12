package com.wix

import com.wix.shepherd.utils.ScalaProperties.javaProperties
import kafka.server.{KafkaConfig, KafkaServer}

import scala.reflect.io.Directory
import scala.util.Try

class EmbeddedKafka(port: Int, zookeeperPort: Int) {
  private val kafkaLogsDir = s"target/kafka/logs/$port/"
  private val brokerId: Int = (Math.random() * 500).toInt
  private val kafkaConfig = KafkaConfig.fromProps(javaProperties(
    "port" → port.toString,
    "log.dir" → kafkaLogsDir,
    "zookeeper.connect" → s"localhost:$zookeeperPort",
    "broker.id" → brokerId.toString,
    "num.partitions" → "8",
    "host.name" → "127.0.0.1"))

  private val kafkaServer: KafkaServer = new KafkaServer(kafkaConfig)

  val address = s"localhost:$port"

  def start() = {
    deleteKafkaLogs()
    kafkaServer.startup()
  }

  def stop() = Try {
    kafkaServer.shutdown()
    kafkaServer.awaitShutdown()
  }

  private def deleteKafkaLogs() = Directory(kafkaLogsDir).deleteRecursively()
}