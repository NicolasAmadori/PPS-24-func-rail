package model.entities

import model.entities.EntityCodes.{StationCode, TrainCode, PassengerCode}

/** Represents a generic passenger.
  */
trait Passenger:
  def code: PassengerCode
  def departure: StationCode
  def destination: StationCode
  def itinerary: Option[Itinerary]

/** Concrete implementation of a passenger.
  *
  * @param code
  *   Passenger code
  * @param departure
  *   Departure station
  * @param destination
  *   Destination station
  * @param itinerary
  *   Optional itinerary
  */
case class PassengerImpl(
    code: PassengerCode,
    departure: StationCode,
    destination: StationCode,
    itinerary: Option[Itinerary]
) extends Passenger

enum PassengerPosition:
  case AtStation(station: StationCode)
  case OnTrain(train: TrainCode)
