package model

trait Train:
  def code: Int
  def speed: Float

case class NormalTrain(code: Int, speed: Float) extends Train

case class HighSpeedTrain(code: Int, speed: Float) extends Train

object Train:
  val normalSpeed: Float = 80.0f
  val highSpeed: Float = 300.0f

  def normalTrain(code: Int): NormalTrain = NormalTrain(code, normalSpeed)

  def highSpeedTrain(code: Int): HighSpeedTrain = HighSpeedTrain(code, highSpeed)
