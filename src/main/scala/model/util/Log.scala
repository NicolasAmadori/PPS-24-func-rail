package model.util

import model.entities.EntityCodes.{PassengerCode, RailCode, StationCode, TrainCode}
import model.entities.{Itinerary, ItineraryLeg, Passenger}

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
  case GetOnTrain(passenger: PassengerCode, previousLeg: ItineraryLeg, nextLeg: ItineraryLeg)
  case GetOffTrain(passenger: PassengerCode, previousLeg: ItineraryLeg, nextLeg: ItineraryLeg)
  case EndTrip(passenger: Passenger)

  override def toString: String = this match
    case StartTrip(passenger) =>
      if passenger.itinerary.isDefined then
        s"Passenger ${passenger.id} started trip: ${passenger.itinerary}"
      else
        s"Passenger ${passenger.id} started trip from ${passenger.departure} to ${passenger.destination} but no itinerary is possible."
    case GetOnTrain(passenger, previousLeg, nextLeg) =>
      s"Passenger $passenger got on train ${nextLeg.train.code} from station ${previousLeg.to}"
    case GetOffTrain(passenger, previousLeg, nextLeg) =>
      s"Passenger $passenger got off train ${previousLeg.train.code} at station ${previousLeg.to} and will wait for train ${nextLeg.train.code}"
    case EndTrip(passenger) =>
      s"Passenger $passenger ended trip: ${passenger.itinerary.get}"