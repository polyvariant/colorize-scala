package org.polyvariant.colorize

private[colorize] object EnvPlatform {
  def get(name: String): Option[String] = Option(System.getenv(name))
}
