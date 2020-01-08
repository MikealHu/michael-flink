package cn.michael.flink.examples.streaming

import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer

/**
 * Created by hufenggang on 2020/1/8.
 */
object KafkaSourceTest2 {

    def main(args: Array[String]): Unit = {

        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

        // 每5000毫秒设置一个checkpoint
        env.enableCheckpointing(5000)

        val properties = new Properties()
        properties.setProperty("bootstrap.servers", "")
        properties.setProperty("zookeeper.connect", "")
        properties.setProperty("group.id", "")

        val consumer = new FlinkKafkaConsumer[String]("stream1", new SimpleStringSchema(), properties)

        // 从最早一条记录开始
        consumer.setStartFromEarliest()
        // 从最后一条记录开始
        consumer.setStartFromLatest()
        // 从指定的时间戳开始（毫秒值）
        consumer.setStartFromTimestamp(0L)
        // 默认值
        consumer.setStartFromGroupOffsets()

        val stream = env
            .addSource(consumer)
            .print()

        env.execute("KafkaSourceTest2")
    }

}
