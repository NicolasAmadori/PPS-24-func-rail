package model.simulation

import model.railway.Domain.StationCode
import model.railway.Railway

case class Simulation(railway: Railway, state: SimulationState):
  def addTrain(train: Train): Either[SimulationError, Simulation] =
    if canAddTrain(train) then
      val updatedState = state.addTrain(train)
      Right(copy(state = updatedState))
    else
      Left(SimulationError.InvalidRoute())

  private def canAddTrain(train: Train): Boolean = train.stations match
    case Nil => false
    case _ => hasValidStations(train.stations)

  private def hasValidStations(stations: List[StationCode]): Boolean =
    stations.forall(station => railway.stations.exists(_.code == station))

object Simulation:
  def withRailway(railway: Railway): Simulation = Simulation(railway, SimulationState.empty)
