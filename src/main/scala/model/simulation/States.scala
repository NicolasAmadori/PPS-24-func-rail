package model.simulation

import model.entities.EntityCodes.{PassengerCode, RailCode, StationCode, TrainCode}
import model.entities.{Passenger, PassengerPosition, PassengerState, Rail, Train, Route}
import model.simulation.TrainPosition.{AtStation, OnRail}
import model.simulation.TrainState.InitialRouteIndex
import model.util.{PassengerGenerator, PassengerLog, TrainLog}
import model.util.TrainLog.{EnteredStation, LeavedStation, WaitingAt}

/** Represent the mutable state of the simulation.
  * @param trains
  * @param trainStates
  *   map of [[model.entities.EntityCodes.TrainCode]] and corresponding state
  * @param railStates
  *   occupancy of rails
  * @param passengers
  *   The list of passenger present in the simulation
  * @param passengerStates
  *   map of [[model.entities.EntityCodes.PassengerCode]] and corresponding state
  * @param simulationStep
  *   counter for simulation progression
  */
case class SimulationState(
    trains: List[Train],
    trainStates: Map[TrainCode, TrainState],
    railStates: Map[RailCode, RailState],
    passengers: List[Passenger],
    passengerStates: Map[PassengerCode, PassengerState],
    simulationStep: Int = -1
):
  /** Assign new trains to the simulation state, creates a trainState entry for each.
    * @param newTrains
    *   trains to be added
    * @return
    *   the updated simulation state
    */
  def withTrains(newTrains: List[Train]): SimulationState =
    val newTrainStates = newTrains.map(_.code -> TrainState()).toMap
    copy(trains = newTrains, trainStates = newTrainStates)

  /** Updates simulation state moving trains of a step and managing rails occupancies
    * @return
    *   the updated simulation state and the list of logs
    */
  def updateTrains(): (SimulationState, List[TrainLog]) =
    val currentState = this
    trainStates.foldLeft((currentState, List.empty)) { (acc, ts) =>
      val (state, logs) = acc
      val (code, trainState) = (ts._1, ts._2)
      val (newTrainState, newPosition) = trainState.update(trains.find(code == _.code).get, state.railStates)
      val updatedTrainStates = state.trainStates.updated(code, newTrainState)
      val (updatedRailStates, log) = updateRailStateOn(ts._1, state.railStates, trainState.position, newPosition)
      (state.copy(trainStates = updatedTrainStates, railStates = updatedRailStates), appendLog(logs, log))
    }

  /** Updates the passengers' state in the simulation.
    *
    * @return
    *   a tuple containing:
    *   - the updated [[SimulationState]]
    *   - the list of [[model.util.PassengerLog]] entries produced during the update
    */
  def updatePassengers(): (SimulationState, List[PassengerLog]) =
    val (newState1, boardingLogs) = boardPassengers()
    val (newState2, deboardingLogs) = newState1.deboardPassengers()
    val (newState3, waitingLogs) = newState2.waitPassengers()
    (newState3, boardingLogs ++ deboardingLogs ++ waitingLogs)

  /** Collects all passengers currently waiting at a station.
    *
    * @return
    *   a map associating each [[model.entities.Passenger]] to the [[model.entities.EntityCodes.StationCode]] of the
    *   station where they are currently located
    */
  private def passengersWaitingAtStations: Map[Passenger, StationCode] =
    (for
      p <- passengers
      if p.itinerary.isDefined
      state <- passengerStates.get(p.code)
      station <- state.currentPosition match
        case PassengerPosition.AtStation(s) => Some(s)
        case _ => None
    yield p -> station).toMap

  /** Collects all passengers currently on board a train.
    *
    * @return
    *   a map associating each [[model.entities.Passenger]] to the [[model.entities.EntityCodes.TrainCode]] of the train
    *   where they are currently located
    */
  private def passengersOnTrains: Map[Passenger, TrainCode] =
    (for
      p <- passengers
      if p.itinerary.isDefined
      state <- passengerStates.get(p.code)
      train <- state.currentPosition match
        case PassengerPosition.OnTrain(t) => Some(t)
        case _ => None
    yield p -> train).toMap

  /** Collects all trains that are currently located at a station.
    *
    * @return
    *   a map associating each [[model.entities.EntityCodes.TrainCode]] to the
    *   [[model.entities.EntityCodes.StationCode]] of the station where it is currently located
    */
  private def trainsAtStations: Map[TrainCode, StationCode] =
    trainStates.collect { case (code, TrainStateImpl(Some(AtStation(s)), _, _, _, _, _)) => code -> s }

  /** Determines which passengers are ready to board a train.
    *
    * A passenger is considered "ready to board" if:
    *   - they are currently waiting at a station
    *   - their itinerary defines a leg starting from that station
    *   - the corresponding train is currently at that station
    *   - the train exists in [[trainStates]]
    *   - the train’s current travel direction matches the itinerary leg’s expected direction
    *
    * @return
    *   a map associating each [[model.entities.EntityCodes.PassengerCode]] to the
    *   [[model.entities.EntityCodes.TrainCode]] of the train they are ready to board
    */
  private def passengerReadyToGetOnTrain: Map[PassengerCode, TrainCode] =
    passengersWaitingAtStations.flatMap { (p, s) =>
      for
        it <- p.itinerary
        leg <- it.legs.find(_.from == s)
        if trainsAtStations.get(leg.train.code).contains(s) &&
          trainStates.contains(leg.train.code) &&
          trainStates(leg.train.code).forward == leg.isForwardRoute
      yield p.code -> leg.train.code
    }

  /** Determines which passengers are ready to leave a train.
    *
    * A passenger is considered "ready to deboard" if:
    *   - they are currently on a train
    *   - their itinerary defines a leg involving the current train
    *   - the train is currently located at the passenger’s destination station for that leg
    *   - the train exists in [[trainStates]]
    *   - the train’s current travel direction matches the itinerary leg’s expected direction
    *
    * @return
    *   a map associating each [[model.entities.EntityCodes.PassengerCode]] to the
    *   [[model.entities.EntityCodes.StationCode]] of the station where they should get off
    */
  private def passengerReadyToGetOffTrain: Map[PassengerCode, StationCode] =
    passengersOnTrains.flatMap { (p, t) =>
      for
        it <- p.itinerary
        leg <- it.legs.find(_.train.code == t)
        if trainsAtStations.get(leg.train.code).contains(leg.to) &&
          trainStates.contains(leg.train.code)
      yield p.code -> leg.to
    }

  /** Moves passengers from stations onto trains when a matching train is available at the station.
    *
    * @return
    *   a tuple containing:
    *   - the updated [[SimulationState]] with passengers moved onto trains
    *   - the list of logs generated
    */
  private def boardPassengers(): (SimulationState, List[PassengerLog]) =
    val newPassengerStates: Map[PassengerCode, PassengerState] =
      passengerStates.map { (pCode, oldState) =>
        passengerReadyToGetOnTrain.get(pCode) match
          case Some(tCode) => pCode -> oldState.changePosition(PassengerPosition.OnTrain(tCode))
          case None => pCode -> oldState
      }

    val newPassengerLogs: List[PassengerLog] = passengerReadyToGetOnTrain.map((pCode, tCode) =>
      PassengerLog.GetOnTrain(pCode, tCode)
    ).toList

    (copy(passengerStates = newPassengerStates), newPassengerLogs)

  /** Moves passengers from trains to stations when they have reached their destination stop.
    *
    * @return
    *   a tuple containing:
    *   - the updated [[SimulationState]] with passengers moved to stations
    *   - the list of logs generated
    */
  private def deboardPassengers(): (SimulationState, List[PassengerLog]) =

    val newPassengerStates: Map[PassengerCode, PassengerState] =
      passengerStates.map { (pCode, oldState) =>
        passengerReadyToGetOffTrain.get(pCode) match
          case Some(sCode) => pCode -> oldState.changePosition(PassengerPosition.AtStation(sCode))
          case None => pCode -> oldState
      }

    val newPassengerLogs: List[PassengerLog] = passengerReadyToGetOffTrain.map((pCode, sCode) =>
      PassengerLog.GetOffTrain(pCode, sCode)
    ).toList

    (copy(passengerStates = newPassengerStates), newPassengerLogs)

  /** Keeps non-moving passengers in their current state.
    *
    * are kept in their current position. Their state is refreshed to maintain a step-by-step history of positions, even
    * if they did not change their position.
    *
    * @return
    *   a tuple containing:
    *   - the updated [[SimulationState]] with "waiting" passengers' states refreshed
    *   - the list of logs generated
    */
  private def waitPassengers(): (SimulationState, List[PassengerLog]) =
    val notMovingPassengers: List[PassengerCode] =
      passengers
        .map(_.code)
        .filterNot(passengerReadyToGetOnTrain.contains)
        .filterNot(passengerReadyToGetOffTrain.contains)

    val newPassengerStates: Map[PassengerCode, PassengerState] =
      passengerStates
        .map { (pCode, oldState) =>
          if notMovingPassengers.contains(pCode) then
            // Reset position to maintain an history step by step of the position
            pCode -> oldState.changePosition(oldState.currentPosition)
          else
            pCode -> oldState
        }

    (copy(passengerStates = newPassengerStates), List.empty)

  def generatePassengers(passengerGenerator: PassengerGenerator)(n: Int)
      : (SimulationState, PassengerGenerator, List[PassengerLog]) =
    val (newGenerator, newPassengers, newPassengersLogs) =
      passengerGenerator.generate(n)
    (
      copy(
        passengers = passengers ++ newPassengers.map(p => p._1),
        passengerStates = passengerStates ++ newPassengers.map(pS => pS._1.code -> pS._2).toMap
      ),
      newGenerator,
      newPassengersLogs
    )

  private def appendLog(logs: List[TrainLog], log: Option[TrainLog]): List[TrainLog] =
    log match
      case Some(l) => logs :+ l
      case _ => logs

  private def updateRailStateOn(
      trainCode: TrainCode,
      states: Map[RailCode, RailState],
      oldPosition: Option[TrainPosition],
      newPosition: TrainPosition
  ): (Map[RailCode, RailState], Option[TrainLog]) =
    (oldPosition, newPosition) match
      case (Some(AtStation(s)), OnRail(r)) =>
        (states.updated(r, states(r).occupyRail), Some(LeavedStation(trainCode, s, r)))
      case (Some(OnRail(r)), AtStation(s)) =>
        (states.updated(r, states(r).freeRail), Some(EnteredStation(trainCode, s)))
      case (Some(AtStation(s)), AtStation(_)) => (states, Some(WaitingAt(trainCode, s)))
      case _ => (states, None)

object SimulationState:
  /** Creates a simulation state with trains */
  def apply(trains: List[Train]): SimulationState = SimulationState(trains, Map.empty, Map.empty, List.empty, Map.empty)

  /** Creates a simulation state with rails occupancy */
  def withRails(rails: List[Rail]): SimulationState =
    val railStates = rails.map(r => r.code -> RailState(r.code)).toMap
    SimulationState(List.empty, Map.empty, railStates, List.empty, Map.empty)

  /** Defines an empty simulation with empty train list */
  def empty: SimulationState = SimulationState(List.empty)

trait TrainState:
  def position: Option[TrainPosition]
  def progress: Int
  def travelTime: Int
  def forward: Boolean
  def previousPositions: List[TrainPosition]
  def update(train: Train, occupancies: Map[RailCode, RailState]): (TrainState, TrainPosition)

case class TrainStateImpl(
    position: Option[TrainPosition],
    progress: Int,
    travelTime: Int,
    previousPositions: List[TrainPosition],
    currentRouteIndex: Int = InitialRouteIndex,
    forward: Boolean = true
) extends TrainState:
  /** Compute new state for train: if it's at station tries to enter next rail, if it's on a rail updates progress and
    * enter a station if arrived
    * @param train
    *   the train to handle
    * @param occupancies
    *   rails occupancies
    * @return
    *   the updated state and the current train position
    */
  def update(train: Train, occupancies: Map[RailCode, RailState]): (TrainState, TrainPosition) =
    position match
      case Some(AtStation(s)) => tryMoveOnRail(train, occupancies)
      case Some(OnRail(r)) => move(train)
      case None => move(train)

  /** Computes new index based on the direction keeping it in route bounds */
  private def nextIndex(routeLength: Int): Int =
    val updated = if forward then currentRouteIndex + 1 else currentRouteIndex - 1
    if updated < 0 then
      0
    else if updated >= routeLength then
      routeLength - 1
    else
      updated

  /** Updates train position to rail if it's free, does nothing otherwise */
  private def tryMoveOnRail(train: Train, occupancies: Map[RailCode, RailState]): (TrainState, TrainPosition) =
    val next = nextIndex(train.route.railsCount)
    val nextRail = train.route.getRailAt(next)
    if occupancies(nextRail.code).free then
      val nextTravelTime = train.getTravelTime(nextRail)
      val nextPosition = OnRail(nextRail.code)
      (
        copy(
          position = Some(nextPosition),
          progress = 1,
          travelTime = nextTravelTime,
          previousPositions = previousPositions :+ position.get,
          currentRouteIndex = next
        ),
        nextPosition
      )
    else (copy(previousPositions = previousPositions :+ position.get), position.get)

  private def newDirectionIfEndOfRoute(position: TrainPosition, route: Route): (Boolean, Int) =
    position match
      case AtStation(s) =>
        if route.isEndOfRoute(s) then (!forward, nextIndex(route.railsCount)) else (forward, currentRouteIndex)
      case _ => throw IllegalStateException()

  /** Updates progress and enter station if it's reached its travel time */
  private def move(train: Train): (TrainState, TrainPosition) =
    val newProgress = progress + 1
    if position.isEmpty then
      val initialPosition = AtStation(train.route.startStation.get)
      (copy(position = Some(initialPosition)), initialPosition)
    else if progress >= travelTime then
      val nextPosition = AtStation(train.route.getEndStationAt(currentRouteIndex, forward))
      val (direction, index) = newDirectionIfEndOfRoute(nextPosition, train.route)
      (
        copy(
          position = Some(nextPosition),
          previousPositions = previousPositions :+ position.get,
          forward = direction,
          currentRouteIndex = index
        ),
        nextPosition
      )
    else
      (copy(progress = newProgress, previousPositions = previousPositions :+ position.get), position.get)

object TrainState:
  val InitialRouteIndex = -1
  def apply(): TrainState = TrainStateImpl(None, 0, 0, List.empty)
  def apply(
      position: TrainPosition
  ): TrainState = TrainStateImpl(Some(position), 0, 0, List.empty)

enum TrainPosition:
  case OnRail(rail: RailCode)
  case AtStation(station: StationCode)

trait RailState:
  def railCode: RailCode
  def free: Boolean
  def freeRail: RailState
  def occupyRail: RailState

case class RailStateImpl(railCode: RailCode, free: Boolean) extends RailState:
  override def freeRail: RailState = copy(free = true)
  override def occupyRail: RailState = copy(free = false)

object RailState:
  def apply(railCode: RailCode): RailState = RailStateImpl(railCode, true)
