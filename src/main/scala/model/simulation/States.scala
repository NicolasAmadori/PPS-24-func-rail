package model.simulation

import model.entities.EntityCodes.{RailCode, StationCode, TrainCode}
import model.entities.{Rail, Train}
import model.simulation.Domain.PassengerCode
import model.simulation.TrainPosition.AtStation

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
  def withTrains(newTrains: List[Train]): SimulationState =
    val newTrainStates = trains.map { t =>
      t.code -> TrainState(t.code, AtStation(t.departureStation))
    }.toMap
    copy(trains = newTrains, trainStates = trainStates ++ newTrainStates)

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
  def progress: Float

case class TrainStateImpl(
    trainCode: TrainCode,
    position: TrainPosition,
    progress: Float,
    currentRouteIndex: Int = 0,
    forward: Boolean = true
) extends TrainState

object TrainState:
  def apply(
      trainCode: TrainCode,
      position: TrainPosition
  ): TrainState = TrainStateImpl(trainCode, position, 0)

enum TrainPosition:
  case OnRail(rail: RailCode)
  case AtStation(station: StationCode)

trait RailState:
  def railCode: RailCode
  def occupied: Boolean

case class RailStateImpl(railCode: RailCode, occupied: Boolean) extends RailState

object RailState:
  def apply(railCode: RailCode): RailState = RailStateImpl(railCode, false)
