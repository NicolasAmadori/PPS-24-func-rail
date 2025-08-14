package model.simulation

import model.entities.EntityCodes.TrainCode
import model.entities.Train
import model.railway.Railway
import model.util.{Log, PassengerGenerator, SimulationLog}
import model.util.SimulationLog.StepExecuted

case class Simulation(duration: Int, railway: Railway, state: SimulationState, passengerGenerator: PassengerGenerator):

  private val INITIAL_PASSENGER_NUMBER = 5
  private val NEW_STEP_PASSENGER_NUMBER = 1

  def start(): (Simulation, List[Log]) =
    val (newGenerator, initialPassengers, initialPassengersLogs) =
      passengerGenerator.generate(INITIAL_PASSENGER_NUMBER)
    val newState = state.copy(
      simulationStep = 0,
      passengers = initialPassengers.map(p => p._1),
      passengerStates = initialPassengers.map(pS => pS._1.id -> pS._2).toMap
    )
    (copy(state = newState, passengerGenerator = newGenerator), SimulationLog.SimulationStarted() +: initialPassengersLogs)

  def doStep(): Either[SimulationError, (Simulation, List[Log])] =
    if state.simulationStep < 0 then
      Left(SimulationError.NotStarted())
    else
      val nextStep = state.simulationStep + 1

      val (stateWithTrainsAndRailsUpdated, trainsLogs) = state.updateTrains() // Update trains
      // Update passengers states
      val (stateWithTrainsAndRailsAndPassengerUpdated, passengersLogs) = state.updatePassengers()
      // Generate new passengers
      val (newGenerator, newPassengers, newPassengersLogs) =
        passengerGenerator.generate(NEW_STEP_PASSENGER_NUMBER)

      val newState = stateWithTrainsAndRailsAndPassengerUpdated.copy(
        simulationStep = nextStep,
        passengers = stateWithTrainsAndRailsAndPassengerUpdated.passengers ++ newPassengers.map(p => p._1),
        passengerStates =
          stateWithTrainsAndRailsAndPassengerUpdated.passengerStates ++ newPassengers.map(pS => pS._1.id -> pS._2).toMap
      )
      val logs = StepExecuted(nextStep) +: (trainsLogs ++ passengersLogs ++ newPassengersLogs)
      Right((copy(state = newState, passengerGenerator = newGenerator), logs))

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
    val state = SimulationState.withRails(railway.rails)
    Simulation(duration, railway, state, PassengerGenerator(railway, state.trains))
