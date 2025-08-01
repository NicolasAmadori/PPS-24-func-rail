package utils

trait ErrorMessage:
  override def toString: String =
    val name = super.toString
    name.substring(name.indexOf('$') + 1, name.indexOf('@'))
