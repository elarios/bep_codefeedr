/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codefeedr.buffer

import org.codefeedr.pipeline.PipelineBuilder
import org.codefeedr.stages.utilities.StringType
import org.codefeedr.testUtils.{SimpleSourcePipelineObject, SimpleTransformPipelineObject}
import org.scalatest.{BeforeAndAfter, FunSuite}

class BufferFactoryTest extends FunSuite with BeforeAndAfter {

  val nodeA = new SimpleSourcePipelineObject()
  val nodeB = new SimpleTransformPipelineObject()

  test("Should throw when giving a null object") {
    val pipeline = new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .append(nodeA)
      .append(nodeB)
      .build()

    val factory = new BufferFactory(pipeline, nodeA, null)

    assertThrows[IllegalArgumentException] {
      factory.create[StringType]()
    }
  }

  test("Should give a configured Kafka buffer when buffertype is kafka") {
    val pipeline = new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .append(nodeA)
      .append(nodeB)
      .build()

    val factory = new BufferFactory(pipeline, nodeA, nodeB)


    // created for nodeB sink, so should have subject of nodeB
    val nodeSubject = nodeB.getSinkSubject
    val buffer = factory.create[StringType]()

    assert(buffer.isInstanceOf[KafkaBuffer[StringType]])
  }

}
