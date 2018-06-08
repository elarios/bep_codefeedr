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
package org.codefeedr.plugins.travis.util

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.codefeedr.keymanager.KeyManager
import org.codefeedr.plugins.travis.TravisProtocol.{TravisBuild, TravisBuilds, TravisRepository}
import org.codefeedr.plugins.travis.util.TravisExceptions.{CouldNotExtractException, CouldNotGetResourceException}
import org.codefeedr.stages.utilities.HttpRequester
import org.json4s.ext.JavaTimeSerializers
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, Formats, JValue}
import scalaj.http.Http

/**
  * Used to interact with the travis API
  * @param keyManager KeyManager that contains keys for the travis API
  */
class TravisService(keyManager: KeyManager) extends Serializable {

  lazy implicit val formats: Formats = DefaultFormats ++ JavaTimeSerializers.all

  private val url = "https://api.travis-ci.org"

  /**
    * Returns a function that checks if a given repo is active on Travis.
    * @return True if it is active, false otherwise
    */
  def repoIsActiveFilter: String => Boolean = {
    val filter: String => Boolean = slug => {
      var responseBody = ""
        while (responseBody.isEmpty) {
          try {
            responseBody = getTravisResource("/repo/" + slug.replace("/", "%2F"))
          } catch {
            case e: CouldNotGetResourceException =>
              e.printStackTrace()
              Thread.sleep(1000)
          }
        }

      val isActive = try {
        val active = parse(responseBody).extract[TravisRepository].active.getOrElse(false)
        active
      } catch {
        case _: org.json4s.MappingException =>
          false
      }
      isActive
    }
    filter
  }

  /**
    * Gives a specific page with build information of a give repository on a given branch.
    * @param owner Name of the owner of the repository
    * @param repoName Name of the repository
    * @param branch Name of the branch
    * @param offset How many builds to skip
    * @param limit How many builds to get
    * @return Page with Travis builds
    */
  def getTravisBuilds(owner: String, repoName: String, branch: String = "", offset: Int = 0, limit: Int = 25): TravisBuilds = {
    getTravisBuildsWithSlug(owner + "%2F" + repoName, branch, offset, limit)
  }

  /**
    * Gives a specific page with build information of a give repository on a given branch.
    * @param slug Slug of the repository
    * @param branch Name of the branch
    * @param offset How many builds to skip
    * @param limit How many builds to get
    * @return Page with Travis builds
    */
  def getTravisBuildsWithSlug(slug: String, branch: String = "", offset: Int = 0, limit: Int = 25): TravisBuilds = {

    val url = "/repo/" + slug + "/builds" +
      (if (branch.nonEmpty) "?branch.name=" + branch else "?") +
      "&sort_by=started_at:desc" +
      "&offset=" + offset +
      "&limit=" + limit

    val responseBody = getTravisResource(url)
    val json = parse(responseBody)
    val builds = extract[TravisBuilds](json)
    builds
  }

  /**
    * Requests a build from Travis with a specific id
    * @param buildID Id of the build
    * @return Travis build
    */
  def getBuild(buildID: Int): TravisBuild = {
    val url = "/build/" + buildID

    val responseBody = getTravisResource(url)
    val json = parse(responseBody)

    val build = extract[TravisBuild](json)
    build
  }

  def parse(json: String) = {
    try {
      JsonMethods.parse(json)
    } catch {
      case e: MismatchedInputException => println("body", json); throw e
      case e: Throwable => e.printStackTrace(); println("body", json); throw e
    }
  }

  /**
    * Extracts a case class from a Jvalue and throws an exception if it fails
    * @param json JSon from which a case class should be extracted
    * @tparam A Case class
    * @return Extracted JSon in case class
    */
  def extract[A : Manifest](json: JValue): A = {
    try {
      json.extract[A]
    } catch {
      case _: Throwable =>
        throw CouldNotExtractException("Could not extract case class from JValue: " + json)
    }
  }

  /**
    * Gets the response body from a specified endpoint in the travis API
    * @param endpoint Endpoint
    * @return Body of the Http response
    */
  @throws(classOf[CouldNotGetResourceException])
  def getTravisResource(endpoint: String): String = {
    val response = try {
      val request = Http(url + endpoint).headers(getHeaders)
      new HttpRequester().retrieveResponse(request)
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        throw CouldNotGetResourceException("Could not get the requested resource from: " + url + endpoint)
    }

    if (response.body.isEmpty) throw CouldNotGetResourceException("Getting resource from " + url + endpoint + " returned an empty body")

    response.body
  }

  /**
    * Gets the needed headers for travis API requests
    * @return
    */
  def getHeaders: List[(String, String)] = {
    ("Travis-API-Version", "3") ::
      ("Authorization", "token " + keyManager.request("travis").get.value) ::
      Nil

  }
}


