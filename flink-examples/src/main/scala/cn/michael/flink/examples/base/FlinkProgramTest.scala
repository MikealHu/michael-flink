package cn.michael.flink.examples.base

import org.apache.flink.shaded.guava18.com.google.common.base.Strings
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

/**
 * author: hufenggang
 * email: michael.hu@jollycorp.com
 * date: 2019/12/27 17:08
 *
 * 每个Flink程序都包含如下几部分：
 * 1.获取执行环境
 * 2.加载或创建初始数据
 * 3.数据转换
 * 4.数据存储
 * 5.触发程序执行
 *
 * 通常，您只需要使用getExecutionEnvironment（），因为这将根据上下文执行正确的操作：
 * 如果您在IDE中执行程序或作为常规Java程序执行，它将创建一个本地环境，该环境将在以下位置执行程序 您的本地计算机。
 * 如果您从程序中创建了一个JAR文件，并通过命令行调用它，则Flink集群管理器将执行您的main方法，而getExecutionEnvironment（）将返回一个用于在集群上执行程序的执行环境。
 */
object FlinkProgramTest {

    def main(args: Array[String]): Unit = {

        // StreamExecutionEnvironment是所有Flink程序的基础
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

        // 读取本地文件
        val text: DataStream[String] = env.readTextFile("D:\\file\\words.txt")

       val mapped = text.filter {x => !Strings.isNullOrEmpty(x)}

        mapped.print()
        mapped.writeAsText("D:\\file\\words_copy.txt")

        env.execute("FlinkProgramTest")
    }
}
