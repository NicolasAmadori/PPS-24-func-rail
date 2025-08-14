package model.simulation

import model.entities.EntityCodes.TrainCode
import model.entities.Train
import model.entities.Train.highSpeedTrain
import model.railway.Railway
import model.util.{Log, PassengerGenerator}
import model.util.SimulationLog.StepExecuted

case class Simulation(duration: Int, railway: Railway, state: SimulationState):

  private val passengerGenerator = PassengerGenerator(railway, state.trains)

  def start(): Simulation =
    val departureStation = railway.stations.filter(s => s.code.toString == "ST3").head.code
    val arrivalStation = railway.stations.filter(s => s.code.toString == "ST4").head.code

    val t1 = highSpeedTrain("T1", List(departureStation, arrivalStation))
    val train1 = t1.withRoute(RouteHelper.getRouteForTrain(t1, railway).get)
    println(train1.route)

    val itineraries = PassengerGenerator(railway, List(train1)).findAllItineraries(departureStation, arrivalStation)
    itineraries.foreach(println)

//    val initialPassengers = passengerGenerator.generate(INITIAL_PASSENGER_NUMBER)
    val initialPassengers = passengerGenerator.generate(1)
    val newState = state.copy(
      simulationStep = 0,
      passengers = initialPassengers.map(t => t._1),
      passengerStates = initialPassengers.map(t => t._1.id -> t._2).toMap
    )
    copy(state = newState)

  def doStep(): Either[SimulationError, (Simulation, List[Log])] =
    if state.simulationStep < 0 then
      Left(SimulationError.NotStarted())
    else
      val (stateWithTrainsAndRailsUpdated, trainsLogs) = state.updateTrains()
      val nextStep = state.simulationStep + 1
      val logs = StepExecuted(nextStep) +: trainsLogs
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
