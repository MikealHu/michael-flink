package cn.michael.flink.examples.streaming

import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer

/**
 * Created by hufenggang on 2020/1/8.
 *
 * 采用正则表达式匹配topic
 */
object KafkaTopicTest1 {

    def main(args: Array[String]): Unit = {

        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

        val properties = new Properties()
        properties.setProperty("bootstrap.servers", "localhost:9092")
        properties.setProperty("group.id", "test")

        val consumer = new FlinkKafkaConsumer[String](
            java.util.regex.Pattern.compile("test-topic-[0-9]"),
            new SimpleStringSchema,
            properties
        )

        val stream = env
            .addSource(consumer)
            .print()

        env.execute("KafkaTopicTest1")
    }
}
