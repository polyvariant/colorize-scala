package org.polyvariant

import scala.scalajs.js
import scala.util.Try

// See https://github.com/typelevel/cats-effect/blob/series/3.x/std/js/src/main/scala/cats/effect/std/EnvCompanionPlatform.scala
private[polyvariant] object EnvPlatform {

  def get(
    name: String
  ): Option[String] = processEnv.get(name).collect { case value: String => value }

  private def processEnv = Try(js.Dynamic.global.process.env.asInstanceOf[js.Dictionary[Any]])
    .getOrElse(js.Dictionary.empty)
}
