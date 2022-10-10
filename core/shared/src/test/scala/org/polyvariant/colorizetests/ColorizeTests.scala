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

import org.polyvariant.colorize.Colorize
import org.polyvariant.colorize.custom.ConfiguredColorize
import org.polyvariant.colorize.custom.RenderConfig
import org.polyvariant.colorize.string.ColorizedString

class ColorizeTests extends munit.FunSuite {
  private val R = "_RESET_"

  val colorize = new ConfiguredColorize(RenderConfig.ansi.copy(resetString = R))

  import colorize._

  test("wrapped string renders directly") {
    assertEquals(
      "hello".render,
      "hello",
    )
  }

  test("overlay whole string prepends the desired string") {
    assertEquals("hello".overlay("a").render, s"ahello$R")
  }

  test("overlay wraps already set color") {
    assertEquals(
      "hello".overlay("blue").overlay("red").render,
      "hello"
        .overlay("blue")
        .render
        .overlay("red")
        .render,
    )
  }

  test("overlay of composition adds prefix/suffix around each part") {
    assertEquals(
      (ColorizedString.wrap("first") ++ ColorizedString.wrap("second"))
        .overlay("X")
        .render,
      s"Xfirst${R}Xsecond$R",
    )
  }

  test("dropOverlays removes all prefixes/suffixes") {
    assertEquals(
      ("hello".overlay("blue") ++ "world"
        .overlay("red")).overlay("green").dropOverlays.render,
      "helloworld",
    )
  }

  test("interpolator: no colors") {
    assertEquals(
      colorize"aa".render,
      ColorizedString.wrap("aa").render,
    )
  }

  test("interpolator: single color") {
    assertEquals(
      colorize"${"aa".overlay("red")}".render,
      "aa".overlay("red").render,
    )
  }

  test("interpolator: multiple colors") {
    assertEquals(
      colorize"${"aa".overlay("red")} and ${"bb".overlay("blue")}".render,
      ("aa".overlay("red") ++ " and " ++ "bb".overlay("blue")).render,
    )
  }

  test("newlines in colorize interpolator") {
    assertEquals(
      colorize"\n".render,
      "\n",
    )
  }

  test("newlines in more complex colorize interpolator") {
    assertEquals(
      colorize"\n${"aa".overlay("red")}\n${"bb".overlay("blue")}\n".render,
      s"\nredaa${R}\nbluebb$R\n",
    )
  }

  test("newlines in colorized strings") {
    assertEquals(
      "\n".overlay("test").render,
      s"test\n$R",
    )
  }

  test("render RGB overlay in TrueColor mode") {

    object colorizeTrue extends ConfiguredColorize(RenderConfig.trueColor.copy(resetString = R))

    import colorizeTrue._

    assertEquals(
      "text".rgb(255, 0, 0).render,
      s"\u001b[38;2;255;0;0mtext$R",
    )
  }

  test("RGB throws if a value outside of the allowed range is used") {
    intercept[IllegalArgumentException] {
      "text".rgb(256, 0, 0)
    }
  }

  test("ignore RGB overlay in Ansi mode") {
    assertEquals(
      "text".rgb(255, 0, 0).render,
      "text",
    )
  }

  test("default colorize") {
    import org.polyvariant.colorize._

    assertEquals("test".red.render, s"${Console.RED}test${Console.RESET}")
  }

  test("use Colorize typeclass") {
    case class Foo(s: String)
    implicit val c: Colorize[Foo] = foo => foo.s.overlay("red")
    val f = Foo("hello")

    assertEquals(
      colorize"$f".render,
      s"redhello$R",
    )
  }
}
