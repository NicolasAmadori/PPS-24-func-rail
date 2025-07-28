package model.simulation

import model.railway.Domain.{RailCode, TrainCode}

case class SimulationState(trains: List[Train], trainStates: Map[TrainCode, TrainState])
    extends TrainOperations[SimulationState]:
  override def withTrains(newTrains: List[Train]): SimulationState = copy(trains = newTrains)

object SimulationState:
  def apply(trains: List[Train]): SimulationState = SimulationState(trains, Map.empty)

  def empty: SimulationState = SimulationState(Nil, Map.empty)

trait TrainState:
  def trainCode: TrainCode
  def currentRail: RailCode
  def progress: Float
  def trainRoute: TrainRoute

case class TrainStateImpl(
    trainCode: TrainCode,
    currentRail: RailCode,
    progress: Float,
    trainRoute: TrainRoute
) extends TrainState
