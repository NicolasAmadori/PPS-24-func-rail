package model.simulation

import model.entities.EntityCodes.{PassengerCode, RailCode, StationCode, TrainCode}
import model.entities.{Passenger, PassengerPosition, Rail, Route, Train}
import model.simulation.TrainPosition.{AtStation, OnRail}
import model.simulation.TrainState.InitialRouteIndex
import model.util.RailLog.{BecomeFaulty, BecomeRepaired}

import model.util.{PassengerGenerator, PassengerLog, RailLog, TrainLog}
import model.util.TrainLog.{EnteredStation, LeavedStation, WaitingAt}

import scala.util.Random

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
  * @param faultyRails
  *   map of currently faulty rails with their repair countdown
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

  def updateRails(): (SimulationState, List[RailLog]) =
    var logs: List[RailLog] = List.empty
    val newRailsStates: Map[RailCode, RailState] = railStates.map((rCode, rState) =>
      if rState.isFaulty then
        val newState = rState.decrementCountdown
        if !newState.isFaulty then
          logs = logs :+ BecomeRepaired(rCode)
        rCode -> newState
      else
        rCode -> rState
    )
    (
      copy(railStates = newRailsStates),
      logs
    )

  private def generateFaultDuration(maxDuration: Int): Int =
    val numbers = 1 to maxDuration

    val weights = numbers.map(n => 1.0 / n)
    val totalWeight = weights.sum
    val cumulative = weights.scanLeft(0.0)(_ + _).tail
  
    val r = Random.nextDouble() * totalWeight
    numbers(cumulative.indexWhere(r <= _))

  def generateRailFaults(faultProbability: Double, maxFaultDuration: Int): (SimulationState, List[RailLog]) =
    val notFaultyRailsStates = railStates.filter((_, rState) => !rState.isFaulty && rState.isFree)
    if Random.nextDouble() > faultProbability || notFaultyRailsStates.isEmpty then
      (copy(), List.empty)
    else
      val newFaultyRail = Random.shuffle(notFaultyRailsStates).head
      val newRailStates = railStates.map((rCode, rState) =>
        if rCode != newFaultyRail._1 then
          rCode -> rState
        else
          rCode -> rState.setFaulty(generateFaultDuration(maxFaultDuration))
      )
      (
        copy(railStates = newRailStates),
        List(BecomeFaulty(newFaultyRail._1))
      )

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
        .filter(p =>
          // Filter out player arrived at their destination
          if p.itinerary.isEmpty then
            true
          else
            val pState = passengerStates(p.code)
            pState.currentPosition match
              case PassengerPosition.AtStation(station) => station != p.destination
              case _ => true
        )
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
        (states.updated(r, states(r).setOccupied), Some(LeavedStation(trainCode, s, r)))
      case (Some(OnRail(r)), AtStation(s)) =>
        (states.updated(r, states(r).setFree), Some(EnteredStation(trainCode, s)))
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
