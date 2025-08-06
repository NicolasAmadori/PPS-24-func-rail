package controller.simconfig

import model.railway.Railway
import model.simulation.Train.{highSpeedTrain, normalTrain}
import model.simulation.{Simulation, SimulationError}

object SimulationBuilder:
  def build(railway: Railway, configs: List[TrainConfig]): Either[SimulationError, Simulation] =
    val trains = configs.map { c =>
      val stops = List(c.departureStation) ++ c.stops.filterNot(s => s == c.departureStation)
      c.trainType match
        case HighSpeed =>
          highSpeedTrain(c.name, stops)
        case NormalSpeed =>
          normalTrain(c.name, stops)
    }
    Simulation.withRailway(railway).addTrains(trains)
