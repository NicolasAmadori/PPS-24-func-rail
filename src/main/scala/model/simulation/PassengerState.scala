package model.simulation

import model.entities.PassengerPosition

/** Represents the state of a passenger in the simulation, tracking current and past positions.
  *
  * @param currentPosition
  *   the current location of the passenger
  * @param previousPositions
  *   the historical list of previous positions, in order of occurrence
  */
final case class PassengerState(
    currentPosition: PassengerPosition,
    previousPositions: List[PassengerPosition] = Nil
):
  /** Updates the passenger's position and records the previous position.
    *
    * @param newPosition
    *   the new position of the passenger
    * @return
    *   a new [[PassengerState]] reflecting the updated position
    */
  def changePosition(newPosition: PassengerPosition): PassengerState =
    copy(
      currentPosition = newPosition,
      previousPositions = previousPositions :+ currentPosition
    )
