## Kafka Consumer

### The DeserializationSchema

Flink Kafka使用者需要了解如何将Kafka中的二进制数据转换为Java / Scala对象。 DeserializationSchema允许用户指定这样的模式。 每个Kafka消息都
会调用T deserialize（byte [] message）方法，并传递来自Kafka的值。

为了方便起见，Flink提供了如下架构：

1. TypeInformationSerializationSchema (and TypeInformationKeyValueSerializationSchema)。它基于Flink的TypeInformation创建架构。
如果数据同时由Flink写入和读取，这将很有用。 该模式是其他通用序列化方法的高性能Flink特定替代方案。

2. JsonDeserializationSchema (and JSONKeyValueDeserializationSchema)。它将序列化的JSON转换为ObjectNode对象，可以采用
objectNode.get("field").as(Int/String/...)()方法访问字段。KeyValue objectNode包含一个“键”和“值”字段，其中包含所有字段，以及一个可选的
“元数据”字段，用于显示此消息的偏移量/分区/主题。

3. AvroDeserializationSchema。它使用静态提供的模式读取以Avro格式序列化的数据。 它可以从Avro生成的类
（AvroDeserializationSchema.forSpecific（...））推断模式，也可以与具有手动提供的模式的
GenericRecords（AvroDeserializationSchema.forGeneric（...））一起使用。 此反序列化架构期望序列化的记录不包含嵌入式架构。

* 此模式还有一个版本，可以在Confluent Schema Registry中查找作者的模式（用于写入记录的模式）。 使用这些反序列化模式记录，将读取从
Schema Registry中检索并转换为静态提供的模式的记录（通过ConfluentRegistryAvroDeserializationSchema.forGeneric（...）或
ConfluentRegistryAvroDeserializationSchema.forSpecific（...））。

使用这种反序列化模式，需要添加如下依赖：

```xml
<dependency>
  <groupId>org.apache.flink</groupId>
  <artifactId>flink-avro</artifactId>
  <version>1.9.0</version>
</dependency>
```

```xml
<dependency>
  <groupId>org.apache.flink</groupId>
  <artifactId>flink-avro-confluent-registry</artifactId>
  <version>1.9.0</version>
</dependency>
```

### Kafka消费者起始位置配置

**Flink Kafka Consumer可以配置如何确定Kafka分区的起始位置。**

举例：

```scala
val env = StreamExecutionEnvironment.getExecutionEnvironment()

val myConsumer = new FlinkKafkaConsumer08[String](...)
myConsumer.setStartFromEarliest()      // start from the earliest record possible
myConsumer.setStartFromLatest()        // start from the latest record
myConsumer.setStartFromTimestamp(...)  // start from specified epoch timestamp (milliseconds)
myConsumer.setStartFromGroupOffsets()  // the default behaviour

val stream = env.addSource(myConsumer)
```

* setStartFromGroupOffsets(默认该配置)：开始从消费者组（消费者属性中的group.id设置）中的分区读取Kafka brokers（或Kafka0.8的Zookeeper）
中已提交的偏移量。 如果找不到分区的偏移量，则将使用属性中的auto.offset.reset设置。
* setStartFromEarliest() / setStartFromLatest(): 从最早/最新记录开始。
* setStartFromTimestamp(long): 从指定的时间戳开始。 对于每个分区，其时间戳大于或等于指定时间戳的记录将用作开始位置。 如果分区的最新记录早于时
间戳，则将仅从最新记录中读取分区。 在这种模式下，Kafka中已提交的偏移将被忽略，并且不会用作起始位置。

也可以为每个分区指定消费者开始消费的确切偏移量

```scala
val specificStartOffsets = new java.util.HashMap[KafkaTopicPartition, java.lang.Long]()
specificStartOffsets.put(new KafkaTopicPartition("myTopic", 0), 23L)
specificStartOffsets.put(new KafkaTopicPartition("myTopic", 1), 31L)
specificStartOffsets.put(new KafkaTopicPartition("myTopic", 2), 43L)

myConsumer.setStartFromSpecificOffsets(specificStartOffsets)
```

### Kafka消费者和容错机制
启用Flink的checkpointing后，Flink Kafka Consumer将使用topic中的记录，并以一致的方式定期检查点其所有Kafka偏移量以及其他操作的状态。 万一作业
失败，Flink将把流式程序恢复到最新检查点的状态，并从存储在检查点的偏移量开始重新使用Kafka的记录。

要使用容错的Kafka使用者，需要在执行环境中启用拓扑检查点：

```scala
val env = StreamExecutionEnvironment.getExecutionEnvironment()
env.enableCheckpointing(5000) // checkpoint every 5000 msecs
```
如果未启用检查点，则Kafka使用者将定期将偏移量提交给Zookeeper。

### Kafka消费者主题和分区发现

#### 分区

#### 主题

### Kafka消费者偏移量提交配置




