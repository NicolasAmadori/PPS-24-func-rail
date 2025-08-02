package utils

object Converters:

  import scala.collection.mutable
  import scala.collection.immutable.List

  def toImmutableList[A](mutableBuffer: mutable.Buffer[A]): List[A] = {
    mutableBuffer.toList
  }