package org.codefeedr.pipeline.buffer
import java.util.Properties

import org.apache.flink.api.common.serialization.{AbstractDeserializationSchema, DeserializationSchema, SerializationSchema}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer011, FlinkKafkaProducer011}
import org.codefeedr.pipeline.buffer.serialization.{JSONDeserializationSchema, JSONSerializationSchema}

import scala.reflect.ClassTag
import scala.reflect.classTag

import org.apache.flink.api.scala._
//import org.apache.flink.api.common.serialization.{DeserializationSchema, SerializationSchema, SimpleStringSchema}
//import org.apache.flink.api.java.operators.DataSink
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
//import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011.Semantic
//import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer011, FlinkKafkaProducer011}
import org.codefeedr.pipeline.Pipeline
import scala.reflect.Manifest

object KafkaBuffer {
  val HOST = "KAFKA_HOST"
  val BROKER_LIST = "KAFKA_BROKER_LIST"
}

class KafkaBuffer[T <: AnyRef : Manifest](pipeline: Pipeline, topic: String) extends Buffer[T](pipeline) {
  //get type of class
  val inputClassType : Class[T] = classTag[T].runtimeClass.asInstanceOf[Class[T]]

  override def getSource: DataStream[T] = {
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "localhost:9092")

    implicit val typeInfo = TypeInformation.of(inputClassType)

    pipeline.environment.
      addSource(new FlinkKafkaConsumer011[T](topic, new JSONDeserializationSchema[T](), properties))
  }

  override def getSink: SinkFunction[T] = {
    new FlinkKafkaProducer011("localhost:9092", topic, new JSONSerializationSchema[T]())
  }
}