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
package org.codefeedr.pipeline

final case class EmptyPipelineException(private val message: String = "",
                                        private val cause: Throwable = None.orNull)
  extends Exception(message, cause)

final case class NoSourceException(private val message: String = "",
                                   private val cause: Throwable = None.orNull)
  extends Exception(message, cause)

final case class NoSinkException(private val message: String = "",
                                 private val cause: Throwable = None.orNull)
  extends Exception(message, cause)

final case class StageNotFoundException(private val message: String = "",
                                        private val cause: Throwable = None.orNull)
  extends Exception(message, cause)

final case class PipelineListException(private val json: String)
  extends Exception(json, null)

final case class StageIdsNotUniqueException(private val stage: String)
  extends Exception(stage, null)