package model.simulation

import model.railway.Domain.{StationCode, TrainCode}

trait Passenger:
  def departure: StationCode
  def destination: StationCode
  def position: PassengerPosition

case class PassengerImpl(departure: StationCode, destination: StationCode, position: PassengerPosition)
    extends Passenger

enum PassengerPosition:
  case AtStation(station: StationCode)
  case OnTrain(train: TrainCode)
