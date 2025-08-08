package model.simulation

import model.railway.Domain.{RailCode, StationCode, TrainCode}
import model.railway.Rail

case class SimulationState(
    trains: List[Train],
    trainStates: Map[TrainCode, TrainState],
    railStates: Map[RailCode, RailState]
) extends TrainOperations[SimulationState]:
  override def withTrains(newTrains: List[Train]): SimulationState = copy(trains = newTrains)

object SimulationState:
  def apply(trains: List[Train]): SimulationState = SimulationState(trains, Map.empty, Map.empty)
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
      progress: Float,
      trainRoute: TrainRoute
  ): TrainState = TrainStateImpl(trainCode, position, progress, trainRoute)

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

trait RailState:
  def railCode: RailCode
  def occupied: Boolean

case class RailStateImpl(railCode: RailCode, occupied: Boolean) extends RailState

object RailState:
  def apply(railCode: RailCode): RailState = RailStateImpl(railCode, false)
