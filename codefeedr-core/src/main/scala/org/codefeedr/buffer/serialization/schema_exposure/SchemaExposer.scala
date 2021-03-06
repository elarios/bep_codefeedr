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
package org.codefeedr.buffer.serialization.schema_exposure

import org.apache.avro.Schema

trait SchemaExposer {

  /**
    * Stores a schema bound to a subject.
    *
    * @param schema the schema belonging to that topic.
    * @param subject the subject belonging to that schema.
    * @return true if correctly saved.
    */
  def put(schema : Schema, subject : String) : Boolean

  /**
    * Get a schema based on a subject.
    *
    * @param subject the subject the schema belongs to.
    * @return None if no schema is found or an invalid schema. Otherwise it returns the schema.
    */
  def get(subject : String) : Option[Schema]

  /**
    * Deletes a Schema.
    *
    * @param subject the subject the schema belongs to.
    * @return true if successfully deleted, otherwise false.
    */
  def delete(subject : String) : Boolean

  /**
    * Deletes all schemas.
    */
  def deleteAll()

  /**
    * Tries to parse a String into a Schema.
    *
    * @param schemaString the schema string.
    * @return an option of a Schema.
    */
  def parse(schemaString: String) : Option[Schema] = {
    try {
      val schema = new Schema.Parser().parse(schemaString)
      Some(schema)
    } catch {
      case x : Throwable => None
    }
  }
}