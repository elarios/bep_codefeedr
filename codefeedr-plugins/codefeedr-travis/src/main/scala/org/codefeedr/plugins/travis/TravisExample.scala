package org.codefeedr.plugins.travis

import org.apache.flink.streaming.api.scala._
import org.codefeedr.buffer.BufferType
import org.codefeedr.keymanager.StaticKeyManager
import org.codefeedr.pipeline.PipelineBuilder
import org.codefeedr.plugins.github.stages.{GitHubEventToPushEvent, GitHubEventsInput}
import org.codefeedr.plugins.travis.TravisProtocol.TravisBuild
import org.codefeedr.plugins.travis.stages.{TravisFilterActiveReposTransformStage, TravisPushEventBuildInfoTransformStage}

import scala.io.Source

object TravisExample {
  def main(args: Array[String]): Unit = {

    val travisKey = Source.fromInputStream(getClass.getResourceAsStream("/travis_api_key")).getLines.mkString
    val githubKey = Source.fromInputStream(getClass.getResourceAsStream("/github_api_key")).getLines.mkString

    new PipelineBuilder()
      .append(new GitHubEventsInput())
      .append(new GitHubEventToPushEvent())
      .append(new TravisFilterActiveReposTransformStage())
      .append(new TravisPushEventBuildInfoTransformStage(100))
      .append{ x: DataStream[TravisBuild] =>
        x.map{build: TravisBuild => (build.repository.slug, build.commit.sha, build.duration) }.print()
      }
      .setKeyManager(new StaticKeyManager(Map("travis" -> travisKey, "events_source" -> githubKey)))
      .build()
      .startMock()
  }
}
