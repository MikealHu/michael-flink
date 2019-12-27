package cn.michael.flink.examples.base

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

/**
 * author: hufenggang
 * email: michael.hu@jollycorp.com
 * date: 2019/12/27 17:38
 */
object FlinkKeyTupleTest {

    def main(args: Array[String]): Unit = {

        val list = List("hadoop mapreduce", "java scala", "hhah")

        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        val stream: DataStream[String] = env.fromCollection(list)

        val sink = stream.flatMap {_.split(" ")}
            .map {x => (x, 1)}
            .keyBy(0)
            .sum(1)

        sink.print()

        env.execute("FlinkKeyTupleTest")
    }

}
