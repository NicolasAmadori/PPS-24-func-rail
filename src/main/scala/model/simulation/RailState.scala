package model.simulation

import model.entities.EntityCodes.RailCode

trait RailState:
  def railCode: RailCode
  def isFree: Boolean
  def isFaulty: Boolean
  def repairCountdown: Option[Int]

  def setFree: RailState
  def setOccupied: RailState
  def setFaulty(daysToRepair: Int): RailState
  def tickDay: RailState

case class RailStateImpl(
    railCode: RailCode,
    isFree: Boolean,
    isFaulty: Boolean,
    repairCountdown: Option[Int] = None
) extends RailState:

  override def setFree: RailState = copy(isFree = true)
  override def setOccupied: RailState = copy(isFree = false)

  override def setFaulty(daysToRepair: Int): RailState =
    copy(isFaulty = true, repairCountdown = Some(daysToRepair))

  override def tickDay: RailState = repairCountdown match
    case Some(days) if days > 1 => copy(repairCountdown = Some(days - 1))
    case Some(_) => copy(isFaulty = false, repairCountdown = None)
    case None => this

object RailState:
  def apply(railCode: RailCode): RailState = RailStateImpl(railCode, isFree = true, isFaulty = false)
