package model.simulation

import model.entities.EntityCodes.TrainCode
import model.entities.Train
import model.railway.Railway
import model.util.{Log, PassengerGenerator, SimulationLog}
import model.util.SimulationLog.StepExecuted

import scala.util.Random

case class Simulation(
    duration: Int,
    railway: Railway,
    state: SimulationState,
    passengerGenerator: PassengerGenerator,
    faultsEnabled: Boolean = false
):

  private val MAX_PASSENGER_NUMBER: Int = 10
  private val MAX_NEW_STEP_PASSENGER_NUMBER: Int = 3
  private val FAULT_PROBABILITY: Double = 0.05
  private val MAX_FAULT_DURATION: Int = 7 * 24

  def start(): (Simulation, List[Log]) =
    val (newState, newGenerator, newPassengersLogs) =
      state.generatePassengers(passengerGenerator)(Random.nextInt(MAX_PASSENGER_NUMBER + 1))
    (
      copy(
        state = newState.copy(simulationStep = 0),
        passengerGenerator = newGenerator
      ),
      SimulationLog.SimulationStarted() +: newPassengersLogs
    )

  def doStep(): Either[SimulationError, (Simulation, List[Log])] =
    if state.simulationStep < 0 then
      Left(SimulationError.NotStarted())
    else if isFinished then
      Left(SimulationError.Finished())
    else
      Right(update())

  def update(): (Simulation, List[Log]) =
    val nextStep = state.simulationStep + 1

    // Update rails faults and generate new ones
    val (newState0, railsLogs) = if faultsEnabled then
      state.updateRails(FAULT_PROBABILITY, MAX_FAULT_DURATION)
    else
      (state, List.empty)
    // Update trains
    val (newState1, trainsLogs) = newState0.updateTrains(railway.rails)
    // Update passengers states
    val (newState2, passengersLogs) =
      newState1.updatePassengers()
    // Generate new passengers
    val (newState3, newGenerator, newPassengersLogs) =
      newState2.generatePassengers(passengerGenerator)(Random.nextInt(MAX_NEW_STEP_PASSENGER_NUMBER + 1))

    val logs = StepExecuted(nextStep) +: (railsLogs ++ trainsLogs ++ passengersLogs ++ newPassengersLogs)
    (
      copy(
        state = newState3.copy(simulationStep = nextStep),
        passengerGenerator = newGenerator
      ),
      logs
    )

  def isFinished: Boolean = state.simulationStep == duration * 24

  /** Adds the train list to the state initializing the state for each by computing the route.
    * @param trains
    * @return
    *   either a list of errors if train's data are invalid or if the route cannot be computed, or the updated
    *   simulation
    */
  def addTrains(trains: List[Train]): Simulation =
    trains.foreach(t => validateTrain(t))
    copy(
      state = state.withTrains(state.trains ++ trains),
      passengerGenerator = PassengerGenerator(railway, state.trains ++ trains)
    )

  private def validateTrain(train: Train): Unit =
    require(!train.code.isEmpty)
    require(railway.stationCodes.contains(train.departureStation))
    require(train.stations.forall(railway.stationCodes.contains(_)))

object Simulation:
  def withRailway(duration: Int, railway: Railway, faultsEnabled: Boolean = false): Simulation =
    val state = SimulationState.withRails(railway.rails)
    Simulation(duration, railway, state, PassengerGenerator(railway, state.trains))
