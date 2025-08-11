package controller.simconfig

import model.railway.Domain.StationCode
import model.railway.Railway
import model.simulation.SimulationError.{CannotComputeRoute, EmptyTrainName, InvalidDeparture, InvalidRoute}
import model.simulation.Train.{highSpeedTrain, normalTrain}
import model.simulation.{HighSpeedTrain, NormalTrain, RouteHelper, Simulation, SimulationError, Train}
import model.simulation.TrainRoute.toRoute

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
    val configurationErrors = validateConfigurations(railway, configs)
    if configurationErrors.nonEmpty then
      Left(configurationErrors)
    else
      val trains = getTrainsFromConfig(configs)
      val trainsWithRoute = assignRoutes(trains, railway)

      val routeErrors = validateRoutes(trainsWithRoute)
      if routeErrors.nonEmpty then
        Left(routeErrors)
      else
        Right(Simulation.withRailway(duration, railway).addTrains(trainsWithRoute))

  private def validateRoutes(trains: List[Train]): List[SimulationError] =
    trains.foldLeft(List.empty)((a, t) => if t.route.isEmpty then a :+ CannotComputeRoute(t.code) else a)

  private def validateConfigurations(railway: Railway, configs: List[TrainConfig]): List[SimulationError] =
    def hasValidStations(stations: List[StationCode]): Boolean =
      stations.forall(station => railway.stations.exists(_.code == station))

    def hasValidDeparture(departure: StationCode): Boolean =
      departure != StationCode.empty && railway.stations.exists(_.code == departure)

    configs.foldLeft(List.empty)((a, config) =>
      List(
        Option.when(config.name.isEmpty)(EmptyTrainName()),
        Option.when(!hasValidDeparture(config.departureStation))(InvalidDeparture(config.name)),
        Option.when(config.stops.isEmpty || !hasValidStations(config.stops))(InvalidRoute(config.name))
      ).flatten
    )

  private def getTrainsFromConfig(configs: List[TrainConfig]): List[Train] =
    configs.map { c =>
      val stops = List(c.departureStation) ++ c.stops.filterNot(s => s == c.departureStation)
      c.trainType match
        case HighSpeed =>
          highSpeedTrain(c.name, stops)
        case NormalSpeed =>
          normalTrain(c.name, stops)
    }

  private def assignRoutes(trains: List[Train], railway: Railway): List[Train] =
    trains.map(t =>
      t.withRoute(
        (t match
          case n: NormalTrain => RouteHelper.getRouteForTrain(n, railway)
          case hs: HighSpeedTrain => RouteHelper.getRouteForTrain(hs, railway)
        ).getOrElse(Nil).toRoute
      )
    )
