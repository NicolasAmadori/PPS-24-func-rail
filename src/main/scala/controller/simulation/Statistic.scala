package controller.simulation

import model.entities.EntityCodes.{StationCode, TrainCode}
import model.entities.Rail

trait Statistic:
  def getStringStat: String
  def getMeasurementUnit: String = ""

object Statistic:
  case class MostUsedRails(rails: List[Rail]) extends Statistic:
    override def toString: String = "Most used rails"
    def getStringStat: String = rails.mkString(" - ")
  case class AverageTrainWaiting(hours: Double) extends Statistic:
    override def toString: String = "Average train waiting"
    private def asDays: Double = hours / 24
    def getStringStat: String =
      if hours < 24 then f"$hours%.2f" else f"$asDays%.2f"
    override def getMeasurementUnit: String =
      if hours < 24 then "hours" else "days"
  case class MostUsedTrains(trains: List[TrainCode]) extends Statistic:
    override def toString: String = "Most used train"
    def getStringStat: String = trains.mkString(" - ")
  case class IncompleteTrips(count: Int) extends Statistic:
    override def toString: String = "Passengers who can't complete the trip"
    def getStringStat: String = count.toString
  case class CompletedTrips(count: Int) extends Statistic:
    override def toString: String = "Passengers who can arrive at destination"
    def getStringStat: String = count.toString
  case class StationsWithMostWaiting(stations: List[StationCode]) extends Statistic:
    override def toString: String = "Stations with most waiting"
    def getStringStat: String = stations.mkString(" - ")
