package view.util

import scala.jdk.javaapi.CollectionConverters

object Converters:

  import scala.collection.immutable.List

  def toImmutableList[A](mutableJavaList: java.util.List[A]): List[A] =
    CollectionConverters.asScala(mutableJavaList).toList
