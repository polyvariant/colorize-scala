/*
 * Copyright 2022 Polyvariant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.polyvariant.colorize

import scala.scalajs.js
import scala.util.Try

// See https://github.com/typelevel/cats-effect/blob/series/3.x/std/js/src/main/scala/cats/effect/std/EnvCompanionPlatform.scala
private[colorize] object EnvPlatform {

  def get(
    name: String
  ): Option[String] = processEnv.get(name).collect { case value: String => value }

  private def processEnv = Try(js.Dynamic.global.process.env.asInstanceOf[js.Dictionary[Any]])
    .getOrElse(js.Dictionary.empty)
}
