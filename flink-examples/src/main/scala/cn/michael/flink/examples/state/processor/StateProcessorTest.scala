package cn.michael.flink.examples.state.processor

import java.lang

import org.apache.flink.api.common.state.MapStateDescriptor
import org.apache.flink.api.scala.typeutils.Types

/**
 * author: hufenggang
 * email: michael.hu@jollycorp.com
 * date: 2019/12/27 16:41
 */
object StateProcessorTest {

    private val CURRENCY_RATES: MapStateDescriptor[Integer, lang.Double] = new MapStateDescriptor("rates", Types.INT, Types.DOUBLE)

    def main(args: Array[String]): Unit = {
    }

}
