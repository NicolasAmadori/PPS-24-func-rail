package model.util

import model.entities.EntityCodes.{RailCode, StationCode, TrainCode}

trait Log

enum SimulationLog extends Log:
  case StepExecuted(step: Int)

  override def toString: String = this match
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
