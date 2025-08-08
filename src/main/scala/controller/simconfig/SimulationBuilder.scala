package controller.simconfig

import model.railway.Railway
import model.simulation.Train.{highSpeedTrain, normalTrain}
import model.simulation.{Simulation, SimulationError}

/** Builder for creating a Simulation instance based on a Railway and a list of TrainConfig.
  */
object SimulationBuilder:

  /** Builds a [[model.simulation.Simulation]] object using the provided [[model.railway.Railway]] and parsing the train
    * configurations.
    * @param railway
    *   the reference Railway
    * @param configs
    *   list of TrainConfig instances that define the trains to be added to the simulation
    * @return
    *   Either a list of SimulationError or a Simulation instance
    */
  def build(duration: Int, railway: Railway, configs: List[TrainConfig]): Either[List[SimulationError], Simulation] =
    val trains = configs.map { c =>
      val stops = List(c.departureStation) ++ c.stops.filterNot(s => s == c.departureStation)
      c.trainType match
        case HighSpeed =>
          highSpeedTrain(c.name, stops)
        case NormalSpeed =>
          normalTrain(c.name, stops)
    }
    Simulation.withRailway(duration, railway).addTrains(trains)
