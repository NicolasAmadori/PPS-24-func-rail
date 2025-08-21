package controller.simulation

import model.entities.EntityCodes.{StationCode, TrainCode}
import model.entities.Rail

trait Statistic:
  def name: String
  def valueAsString: String
  def unit: String = ""
  override def toString: String = name

object Statistic:
  case class MostUsedRails(rails: List[Rail]) extends Statistic:
    val name = "Most used rails"
    def valueAsString: String = rails.mkString(" - ")

  case class AverageTrainWaiting(hours: Double) extends Statistic:
    val name = "Average train waiting"
    private def asDays: Double = hours / 24

    def valueAsString: String =
      if hours < 24 then f"$hours%.2f" else f"$asDays%.2f"
    override def unit: String =
      if hours < 24 then "hours" else "days"

  case class MostUsedTrains(trains: List[TrainCode]) extends Statistic:
    val name = "Most used train"
    def valueAsString: String = trains.mkString(" - ")

  case class IncompleteTrips(count: Int) extends Statistic:
    val name = "Passengers who can't complete the trip"
    def valueAsString: String = count.toString

  case class CompletedTrips(count: Int) extends Statistic:
    val name = "Passengers who can arrive at destination"
    def valueAsString: String = count.toString

  case class StationsWithMostWaiting(stations: List[StationCode]) extends Statistic:
    val name = "Stations with most waiting"
    def valueAsString: String = stations.mkString(" - ")

  case class AverageTripDuration(hours: Double) extends Statistic:
    val name = "Average trip length"
    def valueAsString: String = f"$hours%.2f"
    override def unit: String = "hours"

  case class AveragePassengerWaiting(hours: Double) extends Statistic:
    val name = "Average passenger waiting"
    def valueAsString: String = f"$hours%.2f"
    override def unit: String = "hours"
    
  case class AveragePassengerTravelTime(hours: Double) extends Statistic:
    val name = "Average passenger travel time"
    def valueAsString: String = f"$hours%.2f"
    override def unit: String = "hours"

  def mostUsedBy[A, K](items: List[A], key: A => K): List[A] =
    if items.isEmpty then Nil
    else
      val usage = items.groupBy(key).map { case (_, group) => (group.head, group.size) }
      val maxUsage = usage.values.max
      usage.collect { case (elem, count) if count == maxUsage => elem }.toList

  def consecutiveSameBy[A, K](positions: List[A])(key: A => Option[K]): Double =
    positions.sliding(2).count {
      case List(p1, p2) =>
        (key(p1), key(p2)) match
          case (Some(k1), Some(k2)) if k1 == k2 => true
          case _                                => false
      case _ => false
    }.toDouble