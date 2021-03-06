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
 *
 */
package org.codefeedr.buffer.serialization

import java.nio.charset.StandardCharsets

import org.json4s.NoTypeHints
import org.json4s.ext.JavaTimeSerializers
import org.json4s.jackson.Serialization

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

class JSONSerde[T <: Serializable with AnyRef : TypeTag : ClassTag] extends AbstractSerde[T]{

  //implicit serialization format
  implicit lazy val formats = Serialization.formats(NoTypeHints) ++ JavaTimeSerializers.all

  /**
    * Serializes a (generic) element into a json format.
    *
    * @param element the element to serialized.
    * @return a serialized byte array.
    */
  override def serialize(element: T): Array[Byte] = {
    val string = Serialization.write(element)(formats)
    string.getBytes(StandardCharsets.UTF_8)
  }

  /**
    * Deserializes a (JSON) message into a (generic) case class
    *
    * @param message the message to deserialized.
    * @return a deserialized case class.
    */
  override def deserialize(message: Array[Byte]): T = {
    Serialization.read[T](new String(message, StandardCharsets.UTF_8))
  }
}

object JSONSerde {
  def apply[T <: Serializable with AnyRef : ClassTag : TypeTag]: JSONSerde[T] = new JSONSerde[T]()
}