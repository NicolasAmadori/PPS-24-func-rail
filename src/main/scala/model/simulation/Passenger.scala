package model.simulation

import model.railway.Domain.{StationCode, TrainCode}
import model.railway.Rail
import model.simulation.Domain.PassengerCode

trait Passenger:
  def id: PassengerCode
  def departure: StationCode
  def destination: StationCode
  def chosenRoute: List[Rail]

case class PassengerImpl(id: PassengerCode, departure: StationCode, destination: StationCode, chosenRoute: List[Rail])
    extends Passenger

enum PassengerState:
  case AtStation(station: StationCode)
  case OnTrain(train: TrainCode)
