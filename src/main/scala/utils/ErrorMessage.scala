package utils

trait ErrorMessage

/** Defines a custom error with settable message */
case class CustomError(message: String) extends ErrorMessage:
  override def toString: String = message
