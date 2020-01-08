package cn.michael.flink.examples.streaming

import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}

/**
 * Created by hufenggang on 2020/1/8.
 */
object KafkaSinkTest1 {

    def main(args: Array[String]): Unit = {

        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

        val properties = new Properties()
        properties.setProperty("bootstrap.servers", "")
        properties.setProperty("group.id", "")

        val consumer = new FlinkKafkaConsumer[String]("stream1", new SimpleStringSchema(), properties)

        val stream = env
            .addSource(consumer)

        val producer = new FlinkKafkaProducer[String](
            "localhost:9092",         // broker list
            "my-topic",               // target topic
            new SimpleStringSchema)   // serialization schema

        producer.setWriteTimestampToKafka(true)

        stream.addSink(producer)

        env.execute("KafkaSinkTest1")

    }
}
