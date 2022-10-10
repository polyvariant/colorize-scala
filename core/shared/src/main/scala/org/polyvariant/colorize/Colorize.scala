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

import org.polyvariant.colorize.string.ColorizedString

trait Colorize[A] {
  def colorize(a: A): ColorizedString
}

object Colorize {
  def apply[A](implicit ev: Colorize[A]): Colorize[A] = ev

  implicit val stringColorize: Colorize[String] = ColorizedString.wrap(_)
  implicit val colorizedStringColorize: Colorize[ColorizedString] = identity(_)

  /** A reification of the `Colorize` typeclass. If you see a type mismatch saying a
    * `Colorize.Applied` is required, you're missing a `Colorize[A]`. When you implement it and make
    * it available in implicit scope, an implicit conversion will be applied to your value whenever
    * a Colorize.Applied is necessary.
    */
  final case class Applied(value: ColorizedString) extends AnyVal
}
