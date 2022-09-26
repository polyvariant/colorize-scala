# colorize-scala

Scala microlibrary for ANSI colored strings.

## Installation

```scala
// for normal usage
"org.polyvariant" %% "colorize" % "0.1.1"
// for Scala.js / Scala Native
"org.polyvariant" %%% "colorize" % "0.1.1"
```

## Usage

```scala
import org.polyvariant.colorize._
```

You can colorize any string by calling `.colorize`, `.overlay`, or one of the default helpers (we define one for each color in scala's `AnsiColor` trait):

```scala
"hello".red
// res0: ColorizedString = Overlay(
//   underlying = Wrap(s = "hello"),
//   prefix = "\u001b[31m"
// )
```

If you colorize a string twice, the displayed color will be the one you used first.
This also applies if the string is part of a larger colored string:

```scala
colorize"hello ${"world".blue}.red"
// res1: ColorizedString = Concat(
//   lhs = Wrap(s = "hello "),
//   rhs = Concat(
//     lhs = Wrap(s = ""),
//     rhs = Concat(
//       lhs = Overlay(underlying = Wrap(s = "world"), prefix = "\u001b[34m"),
//       rhs = Wrap(s = ".red")
//     )
//   )
// )
```

This will render as "hello" in red and "world" in blue.

As you've seen, There's a `colorize` string interpolator which allows you to easily nest colorized strings in each other.
You can also combine colorized strings with `++`:

```scala
"hello ".red ++ "world".blue
// res2: ColorizedString = Concat(
//   lhs = Overlay(underlying = Wrap(s = "hello "), prefix = "\u001b[31m"),
//   rhs = Overlay(underlying = Wrap(s = "world"), prefix = "\u001b[34m")
// )
```

## Rendering

To actually render a colorized string, call `.render`.

```scala
println("hello".red.render) // like this
```

You can customize rendering, currently this is limited to passing a custom color suffix.
Internally, `colorize` works by prepending the desired color sequence to your string and appending a suffix, and `render`'s default suffix is `Console.RESET`.

To apply customizations, use `renderConfigured`:

```scala
println("hello".overlay("<RED>").renderConfigured(RenderConfig(resetString = "<RESET>")))
// <RED>hello<RESET>
```

## Color removal

To remove all colors and other overlays from a colorized string, use `.dropOverlays`.
