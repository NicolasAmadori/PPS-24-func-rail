package model.simulation

import model.railway.Domain.{StationCode, TrainCode}
import model.railway.Rail

trait Train:
  def code: TrainCode
  def speed: Float
  def departureStation: StationCode = stations.head
  def stations: List[StationCode]

case class NormalTrain(code: TrainCode, speed: Float, stations: List[StationCode])
    extends Train

case class HighSpeedTrain(code: TrainCode, speed: Float, stations: List[StationCode])
    extends Train

object Train:
  val normalSpeed: Float = 100.0f
  val highSpeed: Float = 300.0f

  def normalTrain(code: String, stations: List[StationCode]): NormalTrain =
    validateRoute(stations)
    NormalTrain(TrainCode(code), normalSpeed, stations)

  def highSpeedTrain(code: String, stations: List[StationCode]): HighSpeedTrain =
    validateRoute(stations)
    HighSpeedTrain(TrainCode(code), highSpeed, stations)

  private def validateRoute(route: List[StationCode]): Unit =
    if route.isEmpty then throw new IllegalArgumentException("Route cannot be empty.")
    if route.distinct.size != route.size then
      throw new IllegalArgumentException("Route cannot contain duplicate stations.")

case class TrainRoute(fullRoute: List[Rail], currentRailIndex: Int = 0, forward: Boolean = true)
