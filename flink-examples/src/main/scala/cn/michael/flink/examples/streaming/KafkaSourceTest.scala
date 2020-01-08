package cn.michael.flink.examples.streaming

import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer

/**
 * Created by hufenggang on 2020/1/8.
 *
 * Kafka数据源测试 demo
 */
object KafkaSourceTest {

    def main(args: Array[String]): Unit = {

        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

        val properties = new Properties()
        properties.setProperty("bootstrap.servers", "")
        properties.setProperty("zookeeper.connect", "")
        properties.setProperty("group.id", "")

        val consumer = new FlinkKafkaConsumer[String]("stream1", new SimpleStringSchema(), properties)

        val stream = env
            .addSource(consumer)
            .print()
    }

}
