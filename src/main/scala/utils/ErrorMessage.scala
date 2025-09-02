package utils

trait ErrorMessage

case class CustomError(message: String) extends ErrorMessage:
  override def toString: String = message
