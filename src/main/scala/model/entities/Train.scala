package model.entities

import model.entities.EntityCodes.{StationCode, TrainCode}

/** Trait to model a train */
trait Train:
  def code: TrainCode
  def speed: Float
  def departureStation: StationCode = stations.head
  def stations: List[StationCode]
  def route: Route

  /** Allows setting a route to the train.
    * @return
    *   the train with the route
    */
  def withRoute(route: Route): Train

  /** @return the time required by a train to cross the given rail */
  def getTravelTime(rail: Rail): Double = rail.length.toDouble / speed.toDouble

case class NormalTrain(code: TrainCode, speed: Float, stations: List[StationCode], route: Route)
    extends Train:
  def withRoute(route: Route): Train = copy(route = route)

case class HighSpeedTrain(code: TrainCode, speed: Float, stations: List[StationCode], route: Route)
    extends Train:
  def withRoute(route: Route): Train = copy(route = route)
  override def getTravelTime(rail: Rail): Double = rail match
    case _: MetalRail => rail.length.toDouble / Train.defaultSpeed.toDouble
    case _: TitaniumRail => super.getTravelTime(rail)

object Train:
  val defaultSpeed: Float = 100.0f
  val highSpeed: Float = 300.0f

  /** Creates a normal train */
  def normalTrain(code: String, stations: List[StationCode]): NormalTrain =
    validateStops(stations)
    NormalTrain(TrainCode(code), defaultSpeed, stations, Route.empty)

  /** Creates a high speed train */
  def highSpeedTrain(code: String, stations: List[StationCode]): HighSpeedTrain =
    validateStops(stations)
    HighSpeedTrain(TrainCode(code), highSpeed, stations, Route.empty)

  private def validateStops(stops: List[StationCode]): Unit =
    if stops.isEmpty then throw new IllegalArgumentException("Route cannot be empty.")
    if stops.distinct.size != stops.size then
      throw new IllegalArgumentException("Route cannot contain duplicate stations.")
