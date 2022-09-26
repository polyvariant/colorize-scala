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

class ColorizeTests extends munit.FunSuite {
  private val R = "_RESET_"

  import org.polyvariant.colorize._

  val testConfig = RenderConfig(resetString = R)

  test("wrapped string renders directly") {
    assertEquals(
      "hello".renderConfigured(testConfig),
      "hello",
    )
  }

  test("overlay whole string prepends the desired string") {
    assertEquals("hello".overlay("a").renderConfigured(testConfig), s"ahello$R")
  }

  test("overlay wraps already set color") {
    assertEquals(
      "hello".overlay("blue").overlay("red").renderConfigured(testConfig),
      "hello"
        .overlay("blue")
        .renderConfigured(testConfig)
        .overlay("red")
        .renderConfigured(testConfig),
    )
  }

  test("overlay of composition adds prefix/suffix around each part") {
    assertEquals(
      (ColorizedString.wrap("first") ++ ColorizedString.wrap("second"))
        .overlay("X")
        .renderConfigured(testConfig),
      s"Xfirst${R}Xsecond$R",
    )
  }

  test("dropOverlays removes all prefixes/suffixes") {
    assertEquals(
      ("hello".overlay("blue") ++ "world"
        .overlay("red")).overlay("green").dropOverlays.renderConfigured(testConfig),
      "helloworld",
    )
  }

  test("interpolator: no colors") {
    assertEquals(
      colorize"aa".renderConfigured(testConfig),
      ColorizedString.wrap("aa").renderConfigured(testConfig),
    )
  }

  test("interpolator: single color") {
    assertEquals(
      colorize"${"aa".overlay("red")}".renderConfigured(testConfig),
      "aa".overlay("red").renderConfigured(testConfig),
    )
  }

  test("interpolator: multiple colors") {
    assertEquals(
      colorize"${"aa".overlay("red")} and ${"bb".overlay("blue")}".renderConfigured(testConfig),
      ("aa".overlay("red") ++ " and " ++ "bb".overlay("blue")).renderConfigured(testConfig),
    )
  }

}
