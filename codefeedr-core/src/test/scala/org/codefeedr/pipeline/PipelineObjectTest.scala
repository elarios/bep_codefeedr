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

package org.codefeedr.pipeline

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.stages.StageAttributes
import org.codefeedr.stages.utilities.StringType
import org.codefeedr.testUtils.SimpleSourcePipelineObject
import org.scalatest.FunSuite

class PipelineObjectTest extends FunSuite {

  class BadSourceObject extends PipelineObject[NoType, StringType] {
    override def transform(source: DataStream[NoType]): DataStream[StringType] = {
      getMainSource()

      null
    }
  }

  class BadSinkObject extends PipelineObject[StringType, NoType] {
    override def transform(source: DataStream[StringType]): DataStream[NoType] = {
      getSink()

      null
    }
  }

  test("Should throw when getting unknown main source") {
    val pipeline = new PipelineBuilder()
        .append(new BadSourceObject())
        .build()

    assertThrows[NoSourceException] {
      pipeline.startMock()
    }
  }

  test("Should throw when getting unknown sink") {
    val pipeline = new PipelineBuilder()
      .append(new BadSinkObject())
      .build()

    assertThrows[NoSinkException] {
      pipeline.startMock()
    }
  }

  test("Setting id attributed propagates") {
    val a = new SimpleSourcePipelineObject(StageAttributes(id = Some("testId")))

    assert(a.id == "testId")
  }
}
