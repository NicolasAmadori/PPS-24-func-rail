package model.entities.dsl

import model.entities.EntityCodes.StationCode
import model.entities.Train.{highSpeedTrain, normalTrain}
import TrainType.{HighSpeed, Normal}
import model.entities.Train
import model.railway.Railway
import model.simulation.RouteHelper

enum TrainType:
  case Normal
  case HighSpeed

class TrainBuilder(private val name: String):
  private var kind: Option[TrainType] = None
  private var departure: Option[StationCode] = None
  private var stops: List[StationCode] = List.empty
  private var railway: Option[Railway] = None

  def ofType(tt: TrainType): TrainBuilder =
    kind = Some(tt)
    this

  def departsFrom(station: StationCode): TrainBuilder =
    departure = Some(station)
    this

  def stopsAt(station: StationCode): TrainBuilder =
    stops = stops :+ station
    this

  def stopsAt(stations: StationCode*): TrainBuilder =
    stops = stops ++ stations
    this

  def in(railway: Railway): TrainBuilder =
    this.railway = Some(railway)
    this

  def build: Train =
    val dep = departure.getOrElse(throw IllegalStateException("Train departure is required"))
    val st =
      Some(
        stops.filterNot(_ == dep)
      ).filterNot(_.isEmpty).getOrElse(throw IllegalStateException("Train must have at least one stop"))
    val allStops = (dep +: st).distinct
    kind match
      case Some(Normal) =>
        val t = normalTrain(name, allStops)
        val route = railway.flatMap(r => RouteHelper.getRouteForTrain(t, r))
        route.map(r => t.withRoute(r)).getOrElse(t)
      case Some(HighSpeed) =>
        val t = highSpeedTrain(name, allStops)
        val route = railway.flatMap(r => RouteHelper.getRouteForTrain(t, r))
        route.map(r => t.withRoute(r)).getOrElse(t)
      case _ => throw IllegalStateException("Train type must be defined")

def train(name: String)(build: TrainBuilder => TrainBuilder): Train =
  build(TrainBuilder(name)).build
