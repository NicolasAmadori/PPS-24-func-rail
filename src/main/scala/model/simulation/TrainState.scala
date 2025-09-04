package model.simulation

import model.entities.EntityCodes.{RailCode, StationCode}
import model.entities.{Rail, Route, Train}
import model.simulation.TrainPosition.{AtStation, OnRail}
import model.simulation.TrainState.InitialRouteIndex

/** Trait to model the actual state of a train */
trait TrainState:
  /** The current position of the train */
  def position: Option[TrainPosition]
  /** The progress of the train in the current position */
  def progress: Int
  /** Time required to complete a rail crossing */
  def travelTime: Int
  /** The direction the train is following the route */
  def forward: Boolean
  /** History of all the positions of the train */
  def previousPositions: List[TrainPosition]
  def update(train: Train, rails: List[Rail], railsStates: Map[RailCode, RailState]): (TrainState, TrainPosition)

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
    * @param railsStates
    *   rails railsStates
    * @return
    *   the updated state and the current train position
    */
  def update(train: Train, rails: List[Rail], railsStates: Map[RailCode, RailState]): (TrainState, TrainPosition) =
    position match
      case Some(AtStation(s)) => tryMoveOnRail(train, rails, railsStates)
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
  private def tryMoveOnRail(
      train: Train,
      rails: List[Rail],
      railsStates: Map[RailCode, RailState]
  ): (TrainState, TrainPosition) =
    val nextRail = getNextRail(currentRouteIndex, train, rails, railsStates)
    if nextRail.nonEmpty then
      val nextTravelTime = train.getTravelTime(nextRail.get).ceil.toInt
      val nextPosition = OnRail(nextRail.get.code)
      (
        copy(
          position = Some(nextPosition),
          progress = 1,
          travelTime = nextTravelTime,
          previousPositions = previousPositions :+ position.get,
          currentRouteIndex = currentRouteIndex
        ),
        nextPosition
      )
    else (copy(previousPositions = previousPositions :+ position.get), position.get)

  /** Gets the next rail in route or an equivalent rail if the designated is broken */
  private def getNextRail(
      next: Int,
      train: Train,
      rails: List[Rail],
      railsState: Map[RailCode, RailState]
  ): Option[Rail] =
    val railInRoute = train.route.getRailAt(next)
    railsState(railInRoute.code) match
      case v =>
        if v.isFree && !v.isFaulty then
          Some(railInRoute)
        else
          val target = Set(railInRoute.stationA, railInRoute.stationB)
          rails.filter(r => Set(r.stationA, r.stationB) == target)
            .filter(r => railsState(r.code).isFree && !railsState(r.code).isFaulty)
            .filter(r => r.canAcceptTrain(train)).sortBy(_.length).headOption

  private def newDirectionIfEndOfRoute(position: TrainPosition, route: Route): (Boolean, Int) =
    position match
      case AtStation(s) =>
        if route.isEndOfRoute(s) then (!forward, currentRouteIndex) else (forward, nextIndex(route.railsCount))
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
  val InitialRouteIndex = 0
  def apply(): TrainState = TrainStateImpl(None, 0, 0, List.empty)
  def apply(
      position: TrainPosition
  ): TrainState = TrainStateImpl(Some(position), 0, 0, List.empty)

enum TrainPosition:
  case OnRail(rail: RailCode)
  case AtStation(station: StationCode)
