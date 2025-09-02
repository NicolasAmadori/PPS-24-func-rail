package controller.simulation.util

import model.entities.EntityCodes.{RailCode, StationCode, TrainCode}

private val NotAvailable = "N/A"

/** Trait to model statistics */
trait Statistic:
  /** @return
    *   the name of the statistic
    */
  def name: String

  /** @return
    *   a string with its value
    */
  def valueAsString: String

  /** @return
    *   the measurement unit
    */
  def unit: String = ""
  override def toString: String = name

/** Trait to model statistics that returns a time value */
trait TimeStatistic extends Statistic:
  def hours: Option[Double]
  private def asDays: Double = hours.getOrElse(0.0) / 24
  def valueAsString: String = hours.fold(NotAvailable)(h => if h < 24 then f"$h%.2f" else f"$asDays%.2f")
  override def unit: String = hours.fold(super.unit)(h => if h < 24 then "hours" else "days")

/** Trait to model statistic that returns a percentage */
trait PercentageStatistic extends Statistic:
  def decimal: Option[Double]
  def valueAsString: String = decimal.map(v => (v * 100).toString).getOrElse(NotAvailable)
  override def unit: String = "%"

object Statistic:
  /** Statistic of the most used rails by trains */
  case class MostUsedRails(rails: Option[List[RailCode]]) extends Statistic:
    val name = "Most used rails"
    def valueAsString: String = rails.fold(NotAvailable)(_.mkString(" - "))

  /** Statistic of the average time trains spend waiting at stations */
  case class AverageTrainWaiting(hours: Option[Double]) extends TimeStatistic:
    val name = "Average train waiting"

  /** Statistic of the trains that are the most used by passengers */
  case class MostUsedTrains(trains: Option[List[TrainCode]]) extends Statistic:
    val name = "Most used train"
    def valueAsString: String = trains.fold(NotAvailable)(_.mkString(" - "))

  /** Statistic of how many passenger can complete their trip */
  case class IncompleteTrips(decimal: Option[Double]) extends PercentageStatistic:
    val name = "Passengers who can't complete the trip"

  /** Statistic of how many passenger cannot complete their trip */
  case class CompletedTrips(decimal: Option[Double]) extends PercentageStatistic:
    val name = "Passengers who can arrive at destination"

  /** Statistic of the station in which the most passengers wait */
  case class StationsWithMostWaiting(stations: Option[List[StationCode]]) extends Statistic:
    val name = "Stations with most waiting"
    def valueAsString: String = stations.fold(NotAvailable)(_.mkString(" - "))

  /** Statistic of passengers average trip duration */
  case class AverageTripDuration(hours: Option[Double]) extends TimeStatistic:
    val name = "Average trip length"

  /** Statistic of passengers average time they spend waiting */
  case class AveragePassengerWaiting(hours: Option[Double]) extends TimeStatistic:
    val name = "Average passenger waiting"

  /** Statistic of passengers average time they spend on trains */
  case class AveragePassengerTravelTime(hours: Option[Double]) extends TimeStatistic:
    val name = "Average passenger travel time"

  /** Computes the element with the most occurrences, considering ties.
    * @param items
    *   list of elements
    * @param key
    *   function to derive the key
    * @tparam A
    *   type of the key
    * @tparam K
    *   type of the elements
    * @return
    *   the list of the elements with the most occurrences.
    */
  def mostUsedBy[A, K](items: List[A], key: A => K): List[A] =
    if items.isEmpty then Nil
    else
      val usage = items.groupBy(key).map { case (_, group) => (group.head, group.size) }
      val maxUsage = usage.values.max
      usage.collect { case (elem, count) if count == maxUsage => elem }.toList

  /** Retrieves the number of times two consecutive elements are equal. For example: (E1 E1 E1 E2) and (E1 E2 E2 E1 E1)
    * should retrieve 2
    * @param elements
    *   the list of positions
    * @param key
    *   function to derive the key
    * @tparam A
    * @tparam K
    * @return
    *   the total number of repetitions in groups
    */
  def consecutiveSameBy[A, K](elements: List[A])(key: A => Option[K]): Double =
    elements.sliding(2).count {
      case List(p1, p2) =>
        (key(p1), key(p2)) match
          case (Some(k1), Some(k2)) if k1 == k2 => true
          case _ => false
      case _ => false
    }.toDouble
