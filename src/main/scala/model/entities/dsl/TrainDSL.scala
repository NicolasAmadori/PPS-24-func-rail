package model.entities.dsl

import model.entities.EntityCodes.StationCode
import model.entities.Train.{highSpeedTrain, normalTrain}
import TrainType.{HighSpeed, Normal}
import model.entities.{HighSpeedTrain, NormalTrain, Train}
import model.railway.Railway
import model.simulation.RouteHelper

/** Type of the trains to build */
enum TrainType:
  case Normal
  case HighSpeed

/** Builder for trains */
class TrainBuilder(private val name: String):
  private var kind: Option[TrainType] = None
  private var departure: Option[StationCode] = None
  private var stops: List[StationCode] = List.empty
  private var railway: Option[Railway] = None

  /** Sets the type of the train to build */
  infix def ofType(tt: TrainType): TrainBuilder =
    kind = Some(tt)
    this

  /** Sets the departure station of the train to build */
  infix def departsFrom(station: StationCode): TrainBuilder =
    departure = Some(station)
    this

  /** Appends a stop to the list of stops of the train to build */
  infix def stopsAt(station: StationCode): TrainBuilder =
    stops = stops :+ station
    this

  /** Appends the list of stops to the stops of the train to build */
  infix def stopsAt(stations: List[StationCode]): TrainBuilder =
    stops = stops ++ stations
    this

  /** Sets the railway */
  infix def in(railway: Railway): TrainBuilder =
    this.railway = Some(railway)
    this

  /** Builds the train */
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

/** Builds a train applying the given build function */
def train(name: String)(build: TrainBuilder => TrainBuilder): Train =
  build(TrainBuilder(name)).build

/** Builds a normal train applying the given build function */
def buildNormalTrain(name: String)(build: TrainBuilder => TrainBuilder): NormalTrain =
  (build(TrainBuilder(name)) ofType Normal).build
    .asInstanceOf[NormalTrain]
    
/** Builds a high speed train applying the given build function */
def buildHighSpeedTrain(name: String)(build: TrainBuilder => TrainBuilder): HighSpeedTrain =
  (build(TrainBuilder(name)) ofType HighSpeed).build
    .asInstanceOf[HighSpeedTrain]
