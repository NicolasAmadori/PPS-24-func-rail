package model.entities

import model.*
import model.entities.EntityCodes.{RailCode, StationCode}

trait Rail:
  def code: RailCode
  def length: Int
  def stationA: StationCode
  def stationB: StationCode
  def canAcceptTrain(train: Train): Boolean = true

  override def toString: String = s"[$code]: \"$stationA\" <-($length)-> \"$stationB\""

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
  def metalRail(code: Int, length: Int, stationA: String, stationB: String): Rail =
    MetalRail(
      RailCode(code),
      length,
      StationCode(stationA),
      StationCode(stationB)
    )

  def titaniumRail(
      code: Int,
      length: Int,
      stationA: String,
      stationB: String
  ): Rail =
    TitaniumRail(
      RailCode(code),
      length,
      StationCode(stationA),
      StationCode(stationB)
    )
