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

package org.polyvariant.colorizetests

object ColorizeExample extends App {

  import org.polyvariant.colorize._

  val s = colorize"""hello, ${"red".red} world! This ${"text".green} is mostly blue.""".blue

  val s2 =
    "you can also " ++ "add".magenta ++ " plain colored " ++ "strings".blue ++ " without an interpolator."

  val s3 = "things like " ++ "underlined".underlined ++ " and " ++ "bold".bold ++ " text also work."

  val s4 =
    colorize"you can combine modifiers like ${"bold".bold.magenta}/${"underlined".underlined.yellow} with ${"colors".bold.underlined.cyan} too!"

  val s5 =
    colorize"${"R".rgb(255, 0, 0)}${"G".rgb(0, 255, 0)}${"B".rgb(0, 0, 255)} colors are available too!"

  println(s.render)
  println(s2.render)
  println(s3.render)
  println(s4.render)
  println(s5.render)
}
