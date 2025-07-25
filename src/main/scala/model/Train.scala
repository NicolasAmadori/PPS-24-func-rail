package model

import model.Domain.{StationCode, TrainCode}

trait Train:
  def code: TrainCode
  def speed: Float
  def stations: List[StationCode]

case class NormalTrain(code: TrainCode, speed: Float, stations: List[StationCode]) extends Train

case class HighSpeedTrain(code: TrainCode, speed: Float, stations: List[StationCode]) extends Train

object Train:
  val normalSpeed: Float = 100.0f
  val highSpeed: Float = 300.0f

  def normalTrain(code: Int): NormalTrain = NormalTrain(TrainCode.fromInt(code), normalSpeed, List.empty)

  def normalTrainWithStops(code: Int, stations: List[StationCode]): NormalTrain =
    NormalTrain(TrainCode.fromInt(code), normalSpeed, stations)

  def highSpeedTrain(code: Int): HighSpeedTrain = HighSpeedTrain(TrainCode.fromInt(code), highSpeed, List.empty)

  def highSpeedTrainWithStops(code: Int, stations: List[StationCode]): HighSpeedTrain =
    HighSpeedTrain(TrainCode.fromInt(code), highSpeed, stations)
