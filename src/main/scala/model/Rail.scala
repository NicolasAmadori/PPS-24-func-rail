package model

import model.Domain.{RailCode, StationCode, TrainCode}

trait Rail:
  def code: RailCode
  def length: Int
  def stationA: StationCode
  def stationB: StationCode
  def canAcceptTrain(train: Train): Boolean = true

case class MetalRail(code: RailCode, length: Int, stationA: StationCode, stationB: StationCode)
    extends Rail

case class TitaniumRail(
    code: RailCode,
    length: Int,
    stationA: StationCode,
    stationB: StationCode
) extends Rail:
  override def canAcceptTrain(train: Train): Boolean =
    train match
      case _: HighSpeedTrain => true
      case _ => false

object Rail:
  def metalRail(code: Int, length: Int, stationA: String, stationB: String): MetalRail =
    MetalRail(
      RailCode.fromInt(code),
      length,
      StationCode.fromString(stationA),
      StationCode.fromString(stationB)
    )

  def titaniumRail(
      code: Int,
      length: Int,
      stationA: String,
      stationB: String
  ): TitaniumRail =
    TitaniumRail(
      RailCode.fromInt(code),
      length,
      StationCode.fromString(stationA),
      StationCode.fromString(stationB)
    )
