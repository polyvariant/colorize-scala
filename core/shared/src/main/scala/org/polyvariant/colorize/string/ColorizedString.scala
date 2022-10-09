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

package org.polyvariant.colorize.string

import org.polyvariant.colorize.custom._

import scala.io.AnsiColor

sealed trait ColorizedString extends Product with Serializable {

  import ColorizedString._

  def render(implicit conf: ConfiguredColorize): String = conf.renderString(this)

  def ++(another: ColorizedString): ColorizedString = ColorizedString.Concat(this, another)

  def overlay(
    newColor: String
  ): ColorizedString = ColorizedString.Overlay(
    underlying = this,
    color = Color.Ansi(newColor),
  )

  /** Colorize the string with an RGB value. Note: color values must be in the range 0-255.
    * Otherwise, this function will throw an exception.
    *
    * Additionally, for RGB colors to be rendered, you need to use a `colorize` variant that
    * supports them, and to actually see them your terminal needs to have truecolor support.
    *
    * Consult the README of this project for more information.
    */
  def rgb(red: Int, green: Int, blue: Int): ColorizedString = {
    require(red >= 0 && red <= 255, "red must be in range [0, 255]")
    require(green >= 0 && green <= 255, "green must be in range [0, 255]")
    require(blue >= 0 && blue <= 255, "blue must be in range [0, 255]")

    ColorizedString.Overlay(
      underlying = this,
      color = Color.Rgb(red, green, blue),
    )
  }

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

object ColorizedString {
  private[colorize] final case class Wrap(s: String) extends ColorizedString

  private[colorize] final case class Overlay(underlying: ColorizedString, color: Color)
    extends ColorizedString

  private[colorize] final case class Concat(lhs: ColorizedString, rhs: ColorizedString)
    extends ColorizedString

  private[colorize] sealed trait Color extends Product with Serializable

  private[colorize] object Color {
    final case class Ansi(prefix: String) extends Color

    final case class Rgb(red: Int, green: Int, blue: Int) extends Color

  }

  val empty = wrap("")

  def wrap(s: String): ColorizedString = Wrap(s)
}
