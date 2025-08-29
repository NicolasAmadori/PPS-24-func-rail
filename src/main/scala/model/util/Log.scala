package model.util

import model.entities.EntityCodes.{PassengerCode, RailCode, StationCode, TrainCode}
import model.entities.Passenger

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
      val day = step / 24 + 1
      val hour = step % 24
      f"[Day $day%02d Hour $hour%02d]"

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

enum RailLog extends Log:
  case BecomeFaulty(rail: RailCode, duration: Int)
  case BecomeRepaired(rail: RailCode)

  override def toString: String = this match
    case BecomeFaulty(rail, duration) =>
      s"Rail $rail is now faulty and will be out of service for $duration hours"
    case BecomeRepaired(rail) =>
      s"Rail $rail has now been repaired and can be used by trains"

enum PassengerLog extends Log:
  case StartTrip(passenger: Passenger)
  case GetOnTrain(passengerCode: PassengerCode, trainCode: TrainCode)
  case GetOffTrain(passengerCode: PassengerCode, stationCode: StationCode)
  case EndTrip(passengerCode: PassengerCode)

  override def toString: String = this match
    case StartTrip(passenger) =>
      passenger.itinerary.fold(
        s"Passenger ${passenger.code} started trip from ${passenger.departure} to ${passenger.destination} but no itinerary is possible"
      )(it => s"Passenger ${passenger.code} started trip: $it")
    case GetOnTrain(passengerCode, trainCode) =>
      s"Passenger $passengerCode got on train $trainCode"
    case GetOffTrain(passengerCode, stationCode) =>
      s"Passenger $passengerCode got off train at station $stationCode"
    case EndTrip(passengerCode) =>
      s"Passenger $passengerCode ended its trip"
