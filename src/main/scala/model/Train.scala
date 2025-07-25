package model

import model.Domain.TrainCode

trait Train:
  def code: TrainCode
  def speed: Float

case class NormalTrain(code: TrainCode, speed: Float) extends Train

case class HighSpeedTrain(code: TrainCode, speed: Float) extends Train

object Train:
  val normalSpeed: Float = 80.0f
  val highSpeed: Float = 300.0f

  def normalTrain(code: Int): NormalTrain = NormalTrain(TrainCode.fromInt(code), normalSpeed)

  def highSpeedTrain(code: Int): HighSpeedTrain = HighSpeedTrain(TrainCode.fromInt(code), highSpeed)
