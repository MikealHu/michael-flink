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

Flink Kafka Consumer支持发现动态创建的Kafka分区，并使用一次精确的保证来使用它们。 在最初检索分区元数据后（即，当作业开始运行时）发现的所有分区
将从最早的偏移量开始消耗。

默认情况下，禁用分区发现。要启用它，请在提供的属性配置中为flink.partition-discovery.interval-millis设置一个非负值，表示发现间隔（以毫秒为单位）。


#### 主题

### Kafka消费者偏移量提交配置

Flink Kafka Consumer可以配置如何将偏移量提交回Kafka broker（或0.8中的Zookeeper）的行为。 请注意，Flink Kafka Consumer不依靠承诺的偏移量
来提供容错保证。 承诺的偏移量仅是出于监视目的公开用户进度的一种方式。

根据是否为作业启动checkpoint，将配置偏移量提交行为的方式分为两种：
* Checkpointing disabled: 如果禁用了检查点，则Flink Kafka使用者将依赖内部使用的Kafka客户端的自动定期偏移量提交功能。 因此，要禁用或启用偏移
提交，只需在提供的“属性”配置中将enable.auto.commit（或对于Kafka 0.8为auto.commit.enable）/ auto.commit.interval.ms键设置为适当的值。
* Checkpointing enabled: 如果启用了检查点，则Flink Kafka Consumer将在检查点完成时提交存储在检查点状态中的偏移量。 这样可以确保Kafka代理中
的已提交偏移量与检查点状态中的偏移量一致。 用户可以通过在使用者上调用setCommitOffsetsOnCheckpoints（boolean）方法来选择禁用或启用偏移提交
（默认情况下，此行为为true）。 请注意，在这种情况下，将完全忽略“属性”中的自动定期偏移提交设置。

### Kafka消费者和时间戳提取及水印发出

在许多情况下，记录的时间戳（显式或隐式地）嵌入到记录本身中。 另外，用户可能想要周期性地或以不规则的方式（例如，水印）发出水印。 基于Kafka流中包含当前
事件时间水印的特殊记录。 对于这些情况，Flink Kafka Consumer允许指定AssignerWithPeriodicWatermarks或AssignerWithPunctuatedWatermarks。

## Kafka Producer

代码举例：
```scala
val stream: DataStream[String] = ...

val myProducer = new FlinkKafkaProducer011[String](
        "localhost:9092",         // broker list
        "my-topic",               // target topic
        new SimpleStringSchema)   // serialization schema

// versions 0.10+ allow attaching the records' event timestamp when writing them to Kafka;
// this method is not available for earlier Kafka versions
myProducer.setWriteTimestampToKafka(true)

stream.addSink(myProducer)
```

上面的示例演示了创建Flink Kafka Producer将流写入单个Kafka目标主题的基本用法。 对于更高级的用法，还有其他构造函数变体可以提供以下功能：
* 提供自定义属性：生产者允许为内部KafkaProducer提供自定义属性配置。 
* 自定义分区程序：要将记录分配给特定分区，可以向构造函数提供FlinkKafkaPartitioner的实现。 将为流中的每条记录调用此分区程序，以确定应将记录发送
到目标主题的确切分区。
* 高级序列化方案：类似于使用者，生产者还允许使用称为KeyedSerializationSchema的高级序列化方案，该方案允许分别对键和值进行序列化。 它还允许覆盖
目标主题，以便一个生产者实例可以将数据发送到多个主题。

### 生产者分区方案

默认情况下，如果未为Flink Kafka Producer指定自定义分区程序，则生产程序将使用FlinkFixedPartitioner将每个Flink Kafka Producer并行子任务
映射到单个Kafka分区（即，接收器子任务收到的​​所有记录最终都将在 相同的Kafka分区）。

可以通过扩展FlinkKafkaPartitioner类来实现自定义分区程序。 所有Kafka版本的构造函数都允许在实例化生产者时提供自定义分区程序。 请注意，分区器实现
必须是可序列化的，因为它们将在Flink节点之间传输。 另外，请记住，由于作业失败，分区器中的任何状态都将丢失，因为分区器不是生产者检查点状态的一部分。

也可以完全避免使用某种分区程序，只需让Kafka通过其附加键（使用提供的序列化模式为每条记录确定）对已写记录进行分区即可。 为此，请在实例化生产者时提供一
个空的自定义分区程序。 提供null作为自定义分区很重要； 如上所述，如果未指定自定义分区程序，则使用FlinkFixedPartitioner。

### Kafka生产者和容错机制



