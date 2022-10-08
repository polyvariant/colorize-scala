package org.polyvariant

private[polyvariant] object EnvPlatform {
  def get(name: String): Option[String] = Option(System.getenv(name))
}
