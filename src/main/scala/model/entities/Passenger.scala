package model.entities

import model.entities.EntityCodes.{StationCode, TrainCode, PassengerCode}

trait Passenger:
  def code: PassengerCode
  def departure: StationCode
  def destination: StationCode
  def itinerary: Option[Itinerary]

case class PassengerImpl(
    code: PassengerCode,
    departure: StationCode,
    destination: StationCode,
    itinerary: Option[Itinerary]
) extends Passenger

enum PassengerPosition:
  case AtStation(station: StationCode)
  case OnTrain(train: TrainCode)

final case class PassengerState(
    currentPosition: PassengerPosition,
    previousPositions: List[PassengerPosition] = Nil
):
  def changePosition(newPosition: PassengerPosition): PassengerState =
    copy(
      currentPosition = newPosition,
      previousPositions = previousPositions :+ currentPosition
    )
