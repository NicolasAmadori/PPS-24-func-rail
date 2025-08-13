package model.simulation

import model.entities.EntityCodes.TrainCode
import model.entities.Train
import model.railway.Railway
import model.util.PassengerGenerator

case class Simulation(duration: Int, railway: Railway, state: SimulationState):

  private val INITIAL_PASSENGER_NUMBER: Int = 100

  private val passengerGenerator = PassengerGenerator(railway)

  def start(): Simulation =
    val initialPassengers = passengerGenerator.generate(INITIAL_PASSENGER_NUMBER)
    val newState = state.copy(
      simulationStep = 0,
      passengers = initialPassengers.map(t => t._1),
      passengerStates = initialPassengers.map(t => t._1.id -> t._2).toMap
    )
    copy(state = newState)

  def doStep(): Either[SimulationError, (Simulation, List[String])] =
    if state.simulationStep < 0 then
      Left(SimulationError.NotStarted())
    else
      val (stateWithTrainsAndRailsUpdated, trainsLogs) = state.updateTrains()
      val nextStep = state.simulationStep + 1
      val logs = List(s"Step $nextStep executed") ++ trainsLogs
      val newState = stateWithTrainsAndRailsUpdated.copy(simulationStep = nextStep)
      Right((copy(state = newState), logs))

  def isFinished: Boolean = state.simulationStep == duration * 24

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
  def withRailway(duration: Int, railway: Railway): Simulation =
    Simulation(duration, railway, SimulationState.withRails(railway.rails))
