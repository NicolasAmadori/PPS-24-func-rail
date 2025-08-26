package model.simulation

import model.entities.PassengerPosition

final case class PassengerState(
    currentPosition: PassengerPosition,
    previousPositions: List[PassengerPosition] = Nil
):
  def changePosition(newPosition: PassengerPosition): PassengerState =
    copy(
      currentPosition = newPosition,
      previousPositions = previousPositions :+ currentPosition
    )
