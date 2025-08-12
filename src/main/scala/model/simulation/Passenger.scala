package model.simulation

import model.entities.EntityCodes.{StationCode, TrainCode}
import model.simulation.Domain.PassengerCode

trait Passenger:
  def id: PassengerCode
  def departure: StationCode
  def destination: StationCode
  def route: Option[Route]

case class PassengerImpl(id: PassengerCode, departure: StationCode, destination: StationCode, route: Option[Route])
    extends Passenger

enum PassengerState:
  case AtStation(station: StationCode)
  case OnTrain(train: TrainCode)
