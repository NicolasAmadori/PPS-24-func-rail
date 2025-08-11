package model.simulation

import model.railway.Domain.{StationCode, TrainCode}
import model.railway.Railway
import model.simulation.SimulationError.{CannotComputeRoute, EmptyTrainName, InvalidDeparture, InvalidRoute}
import model.simulation.TrainRoute.TrainRoute

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
  def addTrains(trains: List[Train]): Simulation =
    trains.foreach(t => validateTrain(t))
    copy(state = state.withTrains(state.trains ++ trains))

  private def validateTrain(train: Train): Unit =
    require(!train.code.isEmpty)
    require(railway.stationCodes.contains(train.departureStation))
    require(train.stations.forall(railway.stationCodes.contains(_)))

object Simulation:
  def withRailway(duration: Int, railway: Railway): Simulation = Simulation(duration, railway, SimulationState.empty)
