package model.simulation

import model.railway.Domain.{StationCode, TrainCode}
import model.railway.Railway
import model.simulation.SimulationError.{EmptyTrainName, InvalidDeparture, InvalidRoute}

case class Simulation(railway: Railway, state: SimulationState):

  def addTrains(trains: List[Train]): Either[List[SimulationError], Simulation] =
    val invalidTrains = trains.flatMap(validateTrain)
    if invalidTrains.nonEmpty then
      Left(invalidTrains)
    else
      val updatedState = state.withTrains(state.trains ++ trains)
      Right(copy(state = updatedState))

  private def validateTrain(train: Train): List[SimulationError] =
    val codeError = if !hasValidCode(train.code) then Some(EmptyTrainName()) else None
    val departureError = if !hasValidDeparture(train.departureStation) then Some(InvalidDeparture(train.code)) else None
    val routeError =
      if train.stations.isEmpty || !hasValidStations(train.stations) then Some(InvalidRoute(train.code)) else None

    List(codeError, departureError, routeError).flatten

  private def hasValidCode(code: TrainCode): Boolean =
    !code.isEmpty

  private def hasValidStations(stations: List[StationCode]): Boolean =
    stations.forall(station => railway.stations.exists(_.code == station))

  private def hasValidDeparture(departure: StationCode): Boolean =
    departure != StationCode.empty && railway.stations.exists(_.code == departure)

object Simulation:
  def withRailway(railway: Railway): Simulation = Simulation(railway, SimulationState.empty)
