package model.simulation

import model.railway.Domain.{RailCode, StationCode, TrainCode}
import model.railway.{Rail, Railway}
import model.simulation.SimulationError.CannotComputeRoute
import model.simulation.TrainPosition.AtStation

/**
 * Represent the mutable state of the simulation.
 * @param trains
 * @param trainStates map of [[model.Domain.TrainCode]] and corresponding state
 * @param railStates occupancy of rails
 * @param simulationStep counter for simulation progression
 */
case class SimulationState(
    trains: List[Train],
    trainStates: Map[TrainCode, TrainState],
    railStates: Map[RailCode, RailState],
    simulationStep: Int = -1
) extends TrainOperations[SimulationState]:
  override def withTrains(newTrains: List[Train]): SimulationState = copy(trains = newTrains)

  private def initTrainWithRoute(train: TrainCode, route: List[Rail], stops: List[StationCode]): SimulationState =
    val trainState = TrainState(train, AtStation(stops.head), TrainRoute(route, stops))
    copy(trainStates = trainStates + (train -> trainState))

  /** Uses [[simulation.RouteHelper]] to initialize trains with their route.
    *
    * @param railway
    *   the railway on which compute routes
    */
  def assignRoutes(railway: Railway): SimulationState =
    trains.foldLeft(this) { (acc, t) =>
      import RouteStrategy.given
      val route = (t match
        case normal: NormalTrain => RouteHelper.getRouteForTrain(normal, railway)
        case highSpeed: HighSpeedTrain => RouteHelper.getRouteForTrain(highSpeed, railway)
      ).getOrElse(Nil)
      acc.initTrainWithRoute(t.code, route, t.stations)
    }

  /** @return
    *   a list of error for those trains that have empty route
    */
  def routeErrors: List[SimulationError] =
    trainStates.collect {
      case (code, ts) if ts.trainRoute.isEmpty => CannotComputeRoute(code)
    }.toList

object SimulationState:
  /** Creates a simulation state with trains */
  def apply(trains: List[Train]): SimulationState = SimulationState(trains, Map.empty, Map.empty)
  /** Defines a empty simulation with empty train list */
  def empty: SimulationState = SimulationState(List.empty)

trait TrainState:
  def trainCode: TrainCode
  def position: TrainPosition
  def progress: Float
  def trainRoute: TrainRoute

case class TrainStateImpl(
    trainCode: TrainCode,
    position: TrainPosition,
    progress: Float,
    trainRoute: TrainRoute
) extends TrainState

object TrainState:
  def apply(
      trainCode: TrainCode,
      position: TrainPosition,
      trainRoute: TrainRoute
  ): TrainState = TrainStateImpl(trainCode, position, 0, trainRoute)

enum TrainPosition:
  case OnRail(rail: RailCode)
  case AtStation(station: StationCode)

case class TrainRoute(
    fullRoute: List[Rail],
    stops: List[StationCode],
    currentRailIndex: Int = 0,
    forward: Boolean = true
)

object TrainRoute:
  def apply(rails: List[Rail], stops: List[StationCode]): TrainRoute =
    val railsStations = rails.flatMap(r => List(r.stationA, r.stationB)).distinct
    if !stops.forall(s => railsStations.contains(s)) then
      throw new IllegalArgumentException("Some stops are not part of the train route.")
    new TrainRoute(rails, stops)

  extension (tr: TrainRoute)
    def isEmpty: Boolean =
      tr.fullRoute == Nil || tr.stops == Nil

trait RailState:
  def railCode: RailCode
  def occupied: Boolean

case class RailStateImpl(railCode: RailCode, occupied: Boolean) extends RailState

object RailState:
  def apply(railCode: RailCode): RailState = RailStateImpl(railCode, false)
