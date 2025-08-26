package model.simulation

import model.entities.EntityCodes.{RailCode, StationCode}
import model.entities.{Route, Train}
import model.simulation.TrainPosition.{AtStation, OnRail}
import model.simulation.TrainState.InitialRouteIndex

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
