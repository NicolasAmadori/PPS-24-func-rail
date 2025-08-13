package model.simulation

import model.entities.EntityCodes.{PassengerCode, RailCode, StationCode, TrainCode}
import model.entities.{Passenger, PassengerState, Rail, Train}
import model.simulation.TrainPosition.{AtStation, OnRail}
import model.simulation.TrainState.InitialRouteIndex

/** Represent the mutable state of the simulation.
  * @param trains
  * @param trainStates
  *   map of [[model.Domain.TrainCode]] and corresponding state
  * @param railStates
  *   occupancy of rails
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
    val newTrainStates = newTrains.map { t =>
      t.code -> TrainState(t.code, AtStation(t.departureStation))
    }.toMap
    copy(trains = newTrains, trainStates = newTrainStates)

  def updateTrains(): SimulationState =
    val currentState = this
    trainStates.foldLeft(currentState) { (acc, ts) =>
      val (code, trainState) = (ts._1, ts._2)
      val (newTrainState, newPosition) = trainState.update(trains.find(code == _.code).get, acc.railStates)
      val updatedTrainStates = acc.trainStates.updated(code, newTrainState)
      if newPosition != trainState.position then
        val updatedRailStates = (trainState.position, newPosition) match
          case (AtStation(_), OnRail(r)) => acc.railStates.updated(r, acc.railStates(r).occupyRail)
          case (OnRail(r), AtStation(_)) => acc.railStates.updated(r, acc.railStates(r).freeRail)
          case _ => throw IllegalStateException()
        copy(trainStates = updatedTrainStates, railStates = updatedRailStates)
      else
        copy(trainStates = updatedTrainStates)
    }

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
  def trainCode: TrainCode
  def position: TrainPosition
  def progress: Int
  def travelTime: Int
  def update(train: Train, occupancies: Map[RailCode, RailState]): (TrainState, TrainPosition)

case class TrainStateImpl(
    trainCode: TrainCode,
    position: TrainPosition,
    progress: Int,
    travelTime: Int,
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
      case AtStation(s) => tryMoveOnRail(train, occupancies)
      case OnRail(r) => move(train)

  /** Computes new index keeping it in route bounds inverting direction if needed */
  private def nextIndexAndDirection(routeLength: Int): (Int, Boolean) =
    val updated = if forward then currentRouteIndex + 1 else currentRouteIndex - 1
    if updated < 0 then
      (0, true)
    else if updated >= routeLength then
      (routeLength - 1, false)
    else
      (updated, forward)

  /** Updates train position to rail if it's free, does nothing otherwise */
  private def tryMoveOnRail(train: Train, occupancies: Map[RailCode, RailState]): (TrainState, TrainPosition) =
    val (nextIndex, newDirection) = nextIndexAndDirection(train.route.railsCount)
    val nextRail = train.route.getRailAt(nextIndex)
    if occupancies(nextRail.code).free then
      val nextTravelTime = train.getTravelTime(nextRail)
      val nextPosition = OnRail(nextRail.code)
      (copy(trainCode, nextPosition, 1, nextTravelTime, nextIndex, newDirection), nextPosition)
    else (this, position)

  /** Updates progress and enter station if it's reached its travel time */
  private def move(train: Train): (TrainState, TrainPosition) =
    val newProgress = progress + 1
    if progress >= travelTime then
      val nextPosition = AtStation(train.route.getEndStationAt(currentRouteIndex, forward))
      (copy(position = nextPosition), nextPosition)
    else
      (copy(progress = newProgress), position)

object TrainState:
  val InitialRouteIndex = -1
  def apply(
      trainCode: TrainCode,
      position: TrainPosition
  ): TrainState = TrainStateImpl(trainCode, position, 0, 0)

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
