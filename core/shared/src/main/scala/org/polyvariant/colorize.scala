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

package org.polyvariant

import org.polyvariant.colorize.ColorizedString.Concat
import org.polyvariant.colorize.ColorizedString.Overlay
import org.polyvariant.colorize.ColorizedString.Wrap
import scala.io.AnsiColor

object colorize {

  implicit def liftStringToColored(s: String): ColorizedString = ColorizedString.wrap(s)

  implicit class ColorizeStringContext(private val sc: StringContext) extends AnyVal {

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

  sealed trait ColorizedString {
    def render: String = renderConfigured(RenderConfig.Default)

    def renderConfigured(config: RenderConfig): String = {

      val render: (Color, String) => String =
        config.mode match {
          case ColorMode.Ansi =>
            (color, text) =>
              color match {
                case Color.Ansi(prefix) => prefix + text + config.resetString
                case _                  => text
              }

          case ColorMode.TrueColor => (color, text) => color.prefix + text + config.resetString
        }

      def go(self: ColorizedString, currentColors: List[Color]): String =
        self match {
          case Wrap(s) => currentColors.foldLeft(s)((text, color) => render(color, text))
          case Overlay(underlying, color) => go(underlying, color :: currentColors)
          case Concat(lhs, rhs)           => go(lhs, currentColors) + go(rhs, currentColors)
        }

      go(this, currentColors = Nil)

    }

    def ++(another: ColorizedString): ColorizedString = ColorizedString.Concat(this, another)

    def overlay(
      newColor: String
    ): ColorizedString = ColorizedString.Overlay(
      underlying = this,
      color = Color.Ansi(newColor),
    )

    def rgb(red: Int, green: Int, blue: Int): ColorizedString = ColorizedString.Overlay(
      underlying = this,
      color = Color.Rgb(red, green, blue),
    )

    def colored(pickColor: AnsiColor => String): ColorizedString = overlay(pickColor(Console))

    def dropOverlays: ColorizedString =
      this match {
        case Overlay(underlying, _) => underlying.dropOverlays
        case w @ Wrap(_)            => w
        case Concat(lhs, rhs)       => Concat(lhs.dropOverlays, rhs.dropOverlays)
      }

    def black: ColorizedString = colored(_.BLACK)
    def red: ColorizedString = colored(_.RED)
    def green: ColorizedString = colored(_.GREEN)
    def yellow: ColorizedString = colored(_.YELLOW)
    def blue: ColorizedString = colored(_.BLUE)
    def magenta: ColorizedString = colored(_.MAGENTA)
    def cyan: ColorizedString = colored(_.CYAN)
    def white: ColorizedString = colored(_.WHITE)
    def black_b: ColorizedString = colored(_.BLACK_B)
    def red_b: ColorizedString = colored(_.RED_B)
    def green_b: ColorizedString = colored(_.GREEN_B)
    def yellow_b: ColorizedString = colored(_.YELLOW_B)
    def blue_b: ColorizedString = colored(_.BLUE_B)
    def magenta_b: ColorizedString = colored(_.MAGENTA_B)
    def cyan_b: ColorizedString = colored(_.CYAN_B)
    def white_b: ColorizedString = colored(_.WHITE_B)
    def bold: ColorizedString = colored(_.BOLD)
    def underlined: ColorizedString = colored(_.UNDERLINED)
    def blink: ColorizedString = colored(_.BLINK)
    def reversed: ColorizedString = colored(_.REVERSED)
    def invisible: ColorizedString = colored(_.INVISIBLE)
  }

  private[colorize] sealed trait Color {
    def prefix: String
  }

  private[colorize] object Color {
    private[colorize] final case class Ansi(prefix: String) extends Color

    private[colorize] final case class Rgb(red: Int, green: Int, blue: Int) extends Color {
      def prefix: String = s"\u001b[38;2;$red;$green;${blue}m"
    }

  }

  object ColorizedString {
    private[colorize] final case class Wrap(s: String) extends ColorizedString

    private[colorize] final case class Overlay(underlying: ColorizedString, color: Color)
      extends ColorizedString

    private[colorize] final case class Concat(lhs: ColorizedString, rhs: ColorizedString)
      extends ColorizedString

    private[colorize] sealed trait SuffixSetting extends Product with Serializable

    val empty = wrap("")

    def wrap(s: String): ColorizedString = Wrap(s)
  }

  private[colorize] case class CurrentColor(value: String) extends AnyVal

  sealed trait ColorMode

  object ColorMode {
    private[colorize] case object Ansi extends ColorMode
    private[colorize] case object TrueColor extends ColorMode
  }

  final case class RenderConfig(
    mode: ColorMode,
    resetString: String,
  )

  object RenderConfig {
    private val TrueColorFlags = Set("truecolor", "24bit")

    val Default: RenderConfig = RenderConfig(mode = ColorMode.Ansi, resetString = Console.RESET)

    def determine: RenderConfig =
      if (isTrueColor)
        trueColor
      else
        ansi

    val ansi: RenderConfig = RenderConfig(mode = ColorMode.Ansi, resetString = Console.RESET)

    val trueColor: RenderConfig = RenderConfig(
      mode = ColorMode.TrueColor,
      resetString = Console.RESET,
    )

    private def isTrueColor: Boolean = EnvPlatform
      .get("COLORTERM")
      .exists(TrueColorFlags.contains)

  }

}
