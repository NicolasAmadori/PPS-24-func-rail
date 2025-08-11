package model.simulation

import model.railway.Domain.{StationCode, TrainCode}
import model.railway.Railway
import model.simulation.SimulationError.{CannotComputeRoute, EmptyTrainName, InvalidDeparture, InvalidRoute}

case class Simulation(duration: Int, railway: Railway, state: SimulationState):

  def start(): Simulation =
    val newState = state.copy(simulationStep = 0)
    copy(state = newState)

  def doStep(): Either[SimulationError, (Simulation, List[String])] =
    if state.simulationStep < 0 then
      Left(SimulationError.NotStarted())
    else
      val nextStep = state.simulationStep + 1
      val logs = List(s"Step $nextStep executed")
      val newState = state.copy(simulationStep = nextStep)
      Right((copy(state = newState), logs))

  def isFinished: Boolean = state.simulationStep == duration

  /** Adds the train list to the state initializing the state for each by computing the route.
    * @param trains
    * @return
    *   either a list of errors if train's data are invalid or if the route cannot be computed, or the updated
    *   simulation
    */
  def addTrains(trains: List[Train]): Either[List[SimulationError], Simulation] =
    val invalidTrains = trains.flatMap(validateTrain)
    if invalidTrains.nonEmpty then
      Left(invalidTrains)
    else
      val updatedState = state.withTrains(state.trains ++ trains).assignRoutes(railway)
      val errors = updatedState.routeErrors
      if errors.nonEmpty then Left(errors)
      else Right(copy(state = updatedState))

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
  def withRailway(duration: Int, railway: Railway): Simulation = Simulation(duration, railway, SimulationState.empty)
