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

package org.polyvariant.colorize.custom

import org.polyvariant.colorize.string.ColorizedString
import org.polyvariant.colorize.Colorize
import org.polyvariant.colorize.EnvPlatform

class ConfiguredColorize(config: RenderConfig) {

  implicit final val configuredColorize: ConfiguredColorize = this

  implicit final def liftStringToColored(s: String): ColorizedString = ColorizedString.wrap(s)

  implicit final def colorizeToColored[A: Colorize](a: A): ColorizedString = Colorize[A].colorize(a)

  implicit final class ColorizeStringContext(private val sc: StringContext) {

    def colorize(args: ColorizedString*): ColorizedString = {
      // not available in Scala 2.12 - restore when 2.12 support is dropped
      // StringContext.checkLengths(args, sc.parts)
      assert(
        args.length == sc.parts.length - 1,
        "Number of arguments must match number of interpolations",
      )

      val partsEscaped = sc.parts.map(StringContext.processEscapes(_))

      ColorizedString.wrap(partsEscaped.head) ++ args
        .zip(partsEscaped.tail)
        .map { case (p, s) => p ++ ColorizedString.wrap(s) }
        .foldLeft(ColorizedString.empty)(_ ++ _)
    }

  }

  private[colorize] final def renderString(s: ColorizedString): String = {
    import ColorizedString._

    val renderColor: (Color, String) => String =
      config.mode match {
        case RenderConfig.ColorMode.Ansi =>
          (color, text) =>
            color match {
              case Color.Ansi(prefix) => prefix + text + config.resetString
              case _                  => text
            }

        case RenderConfig.ColorMode.TrueColor =>
          (color, text) => {
            val prefix: String =
              color match {
                case Color.Ansi(prefix)          => prefix
                case Color.Rgb(red, green, blue) => s"\u001b[38;2;$red;$green;${blue}m"
              }

            prefix + text + config.resetString
          }
      }

    def go(self: ColorizedString, currentColors: List[Color]): String =
      self match {
        case Wrap(s) => currentColors.foldLeft(s)((text, color) => renderColor(color, text))
        case Overlay(underlying, color) => go(underlying, color :: currentColors)
        case Concat(lhs, rhs)           => go(lhs, currentColors) + go(rhs, currentColors)
      }

    go(s, currentColors = Nil)

  }

}

final case class RenderConfig(
  mode: RenderConfig.ColorMode,
  resetString: String,
)

object RenderConfig {
  private val TrueColorFlags = Set("truecolor", "24bit")

  val ansi: RenderConfig = RenderConfig(
    mode = ColorMode.Ansi,
    resetString = Console.RESET,
  )

  val trueColor: RenderConfig = RenderConfig(
    mode = ColorMode.TrueColor,
    resetString = Console.RESET,
  )

  def auto: RenderConfig =
    if (isTrueColor)
      trueColor
    else
      ansi

  val Default: RenderConfig = ansi

  private def isTrueColor: Boolean = EnvPlatform
    .get("COLORTERM")
    .exists(TrueColorFlags.contains)

  sealed trait ColorMode

  object ColorMode {
    case object Ansi extends ColorMode
    case object TrueColor extends ColorMode
  }

}
