package model.simulation

import model.entities.EntityCodes.RailCode

trait RailState:
  def isFree: Boolean
  def isFaulty: Boolean
  def repairCountdown: Option[Int]

  def setFree: RailState
  def setOccupied: RailState
  def setFaulty(stepToRepair: Int): RailState
  def decrementCountdown: RailState

case class RailStateImpl(
    railCode: RailCode,
    isFree: Boolean,
    isFaulty: Boolean,
    repairCountdown: Option[Int] = None
) extends RailState:

  override def setFree: RailState = copy(isFree = true)
  override def setOccupied: RailState = copy(isFree = false)

  override def setFaulty(stepToRepair: Int): RailState =
    copy(isFaulty = true, repairCountdown = Some(stepToRepair))

  override def decrementCountdown: RailState = repairCountdown match
    case Some(step) if step > 1 => copy(repairCountdown = Some(step - 1))
    case Some(_) => copy(isFaulty = false, repairCountdown = None)
    case None => this

object RailState:
  def apply(railCode: RailCode): RailState = RailStateImpl(railCode, isFree = true, isFaulty = false)
