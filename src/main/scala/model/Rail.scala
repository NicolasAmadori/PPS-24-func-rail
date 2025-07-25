package model

import model.Domain.RailCode

trait Rail:
  def code: RailCode
  def length: Int
  def stationA: Station
  def stationB: Station
  def train: Option[Train]
  def isEmpty: Boolean = train.isEmpty
  def canAcceptTrain(train: Train): Boolean = isEmpty

case class MetalRail(code: RailCode, length: Int, stationA: Station, stationB: Station, train: Option[Train])
    extends Rail

case class TitaniumRail(
    code: RailCode,
    length: Int,
    stationA: Station,
    stationB: Station,
    train: Option[HighSpeedTrain]
) extends Rail:
  override def canAcceptTrain(train: Train): Boolean =
    isEmpty && (train match
      case _: HighSpeedTrain => true
      case _ => false)

object Rail:
  def metalRail(code: Int, length: Int, stationA: Station, stationB: Station, train: Train): MetalRail =
    MetalRail(RailCode.fromInt(code), length, stationA, stationB, Some(train))

  def emptyMetalRail(code: Int, length: Int, stationA: Station, stationB: Station): MetalRail =
    MetalRail(RailCode.fromInt(code), length, stationA, stationB, None)

  def titaniumRail(code: Int, length: Int, stationA: Station, stationB: Station, train: HighSpeedTrain): TitaniumRail =
    TitaniumRail(RailCode.fromInt(code), length, stationA, stationB, Some(train))

  def emptyTitaniumRail(code: Int, length: Int, stationA: Station, stationB: Station): TitaniumRail =
    TitaniumRail(RailCode.fromInt(code), length, stationA, stationB, None)
