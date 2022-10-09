package org.polyvariant

package object colorize
  extends org.polyvariant.colorize.ConfiguredColorize(
    RenderConfig(RenderConfig.ColorMode.Ansi, Console.RESET)
  )
