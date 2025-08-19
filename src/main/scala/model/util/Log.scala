package model.util

import model.entities.EntityCodes.{PassengerCode, RailCode, StationCode, TrainCode}
import model.entities.{ItineraryLeg, Passenger}

trait Log

enum SimulationLog extends Log:
  case SimulationStarted()
  case SimulationFinished()
  case StepExecuted(step: Int)

  override def toString: String = this match
    case SimulationStarted() =>
      "Simulation started"
    case SimulationFinished() =>
      "Simulation finished"
    case StepExecuted(step) =>
      s"Step $step executed"

enum TrainLog extends Log:
  case EnteredStation(train: TrainCode, station: StationCode)
  case LeavedStation(train: TrainCode, station: StationCode, rail: RailCode)
  case WaitingAt(train: TrainCode, station: StationCode)

  override def toString: String = this match
    case EnteredStation(train, station) =>
      s"Train $train entered station $station"
    case LeavedStation(train, station, rail) =>
      s"Train $train leaved station $station on rail $rail"
    case WaitingAt(train, station) =>
      s"Train $train is waiting at station $station"

enum PassengerLog extends Log:
  case StartTrip(passenger: Passenger)
  case GetOnTrain(passenger: PassengerCode, trainCode: TrainCode)
  case GetOffTrain(passenger: PassengerCode, stationCode: StationCode)
  case EndTrip(passenger: Passenger)

  override def toString: String = this match
    case StartTrip(passenger) =>
      if passenger.itinerary.isDefined then
        s"Passenger ${passenger.code} started trip: ${passenger.itinerary.get}"
      else
        s"Passenger ${passenger.code} started trip from ${passenger.departure} to ${passenger.destination} but no itinerary is possible"
    case GetOnTrain(passenger, trainCode) =>
      s"Passenger $passenger got on train $trainCode"
    case GetOffTrain(passenger, stationCode) =>
      s"Passenger $passenger got off train at station $stationCode"
    case EndTrip(passenger) =>
      s"Passenger $passenger ended trip: ${passenger.itinerary.get}"
