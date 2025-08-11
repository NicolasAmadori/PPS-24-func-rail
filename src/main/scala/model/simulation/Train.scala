package model.simulation

import model.railway.Domain.{StationCode, TrainCode}
import model.railway.Rail
import model.simulation.SimulationError.{EmptyTrainName, InvalidDeparture, InvalidRoute}
import model.simulation.TrainRoute.TrainRoute

trait Train:
  def code: TrainCode
  def speed: Float
  def departureStation: StationCode = stations.head
  def stations: List[StationCode]
  def route: TrainRoute
  def withRoute(route: TrainRoute): Train

case class NormalTrain(code: TrainCode, speed: Float, stations: List[StationCode], route: TrainRoute)
    extends Train:
  def withRoute(route: TrainRoute): Train = copy(route = route)

case class HighSpeedTrain(code: TrainCode, speed: Float, stations: List[StationCode], route: TrainRoute)
    extends Train:
  def withRoute(route: TrainRoute): Train = copy(route = route)

object Train:
  val normalSpeed: Float = 100.0f
  val highSpeed: Float = 300.0f

  def normalTrain(code: String, stations: List[StationCode]): NormalTrain =
    validateStops(stations)
    NormalTrain(TrainCode(code), normalSpeed, stations, TrainRoute.empty)

  def highSpeedTrain(code: String, stations: List[StationCode]): HighSpeedTrain =
    validateStops(stations)
    HighSpeedTrain(TrainCode(code), highSpeed, stations, TrainRoute.empty)
    
  private def validateStops(stops: List[StationCode]): Unit =
    if stops.isEmpty then throw new IllegalArgumentException("Route cannot be empty.")
    if stops.distinct.size != stops.size then
      throw new IllegalArgumentException("Route cannot contain duplicate stations.")

object TrainRoute:
  opaque type TrainRoute = List[Rail]
  def apply(rails: List[Rail]): TrainRoute = rails
  def empty: TrainRoute = List.empty

  extension (tr: TrainRoute)
    def isEmpty: Boolean =
      tr.isEmpty

    def stationsInRoute: Set[StationCode] =
      tr.flatMap(r => List(r.stationA, r.stationB)).toSet
  
  extension (rl: List[Rail])
    def toRoute: TrainRoute = rl