package model.entities

import model.*
import model.entities.EntityCodes.{RailCode, StationCode}

/** Trait to define a rail */
trait Rail:
  def code: RailCode

  /** The length */
  def length: Int

  /** The cost of using the rail */
  def cost: Double

  /** One endpoint of the rail */
  def stationA: StationCode

  /** The other endpoint of the rail */
  def stationB: StationCode

  /** @return true if the given train can pass through the rail, false otherwise */
  def canAcceptTrain(train: Train): Boolean = true

  override def toString: String = s"[$code]: \"$stationA\" <-($length)-> \"$stationB\""

case class MetalRail(code: RailCode, length: Int, cost: Double, stationA: StationCode, stationB: StationCode)
    extends Rail

case class TitaniumRail(
    code: RailCode,
    length: Int,
    cost: Double,
    stationA: StationCode,
    stationB: StationCode
) extends Rail:
  override def canAcceptTrain(train: Train): Boolean =
    train match
      case _: HighSpeedTrain => true
      case _ => false

object Rail:
  private val metalRailCostPerKm: Double = 0.09
  private val titaniumRailCostPerKm: Double = 1.9

  /** Creates a metal rail */
  def metalRail(code: String, length: Int, stationA: String, stationB: String): Rail =
    MetalRail(
      RailCode(code),
      length,
      length.toDouble * metalRailCostPerKm,
      StationCode(stationA),
      StationCode(stationB)
    )

  /** Creates a titanium rail */
  def titaniumRail(
      code: String,
      length: Int,
      stationA: String,
      stationB: String
  ): Rail =
    TitaniumRail(
      RailCode(code),
      length,
      length.toDouble * titaniumRailCostPerKm,
      StationCode(stationA),
      StationCode(stationB)
    )
