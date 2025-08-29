package controller.simulation.util

import model.entities.EntityCodes.{RailCode, StationCode, TrainCode}

private val NotAvailable = "N/A"

trait Statistic:
  def name: String
  def valueAsString: String
  def unit: String = ""
  override def toString: String = name

trait HoursStatistic extends Statistic:
  def hours: Option[Double]
  def valueAsString: String = hours.fold(NotAvailable)(h => f"$h%.2f")
  override def unit: String = hours.fold(super.unit)(_ => "hours")

object Statistic:
  case class MostUsedRails(rails: Option[List[RailCode]]) extends Statistic:
    val name = "Most used rails"
    def valueAsString: String = rails.fold(NotAvailable)(_.mkString(" - "))

  case class AverageTrainWaiting(hours: Option[Double]) extends Statistic:
    val name = "Average train waiting"
    private def asDays: Double = hours.getOrElse(0.0) / 24

    def valueAsString: String = hours.fold(NotAvailable)(h => if h < 24 then f"$h%.2f" else f"$asDays%.2f")

    override def unit: String = hours.fold(super.unit)(h => if h < 24 then "hours" else "days")

  case class MostUsedTrains(trains: Option[List[TrainCode]]) extends Statistic:
    val name = "Most used train"
    def valueAsString: String = trains.fold(NotAvailable)(_.mkString(" - "))

  case class IncompleteTrips(count: Option[Int]) extends Statistic:
    val name = "Passengers who can't complete the trip"
    def valueAsString: String = count.map(_.toString).getOrElse(NotAvailable)

  case class CompletedTrips(count: Option[Int]) extends Statistic:
    val name = "Passengers who can arrive at destination"
    def valueAsString: String = count.map(_.toString).getOrElse(NotAvailable)

  case class StationsWithMostWaiting(stations: Option[List[StationCode]]) extends Statistic:
    val name = "Stations with most waiting"
    def valueAsString: String = stations.fold(NotAvailable)(_.mkString(" - "))

  case class AverageTripDuration(hours: Option[Double]) extends HoursStatistic:
    val name = "Average trip length"

  case class AveragePassengerWaiting(hours: Option[Double]) extends HoursStatistic:
    val name = "Average passenger waiting"

  case class AveragePassengerTravelTime(hours: Option[Double]) extends HoursStatistic:
    val name = "Average passenger travel time"

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
          case _ => false
      case _ => false
    }.toDouble
