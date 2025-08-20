package controller.simulation

import model.entities.EntityCodes.{RailCode, TrainCode}
import model.entities.{Itinerary, Rail}
import model.simulation.TrainPosition.{AtStation, OnRail}
import model.simulation.{Simulation, TrainPosition}

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

object ReportGenerator:
  import Statistic.*

  def createReport(simulation: Simulation): List[Statistic] =
    val railsStat = mostUsedRails(simulation.state.trains.flatMap(_.route.rails))
    val averageTrainWaitingStat =
      averageTrainWaiting(simulation.state.trainStates.values.map(_.previousPositions).toList)
    val mostUsedTrains = mostUsedTrain(simulation.state.passengers.map(_.itinerary.get))
    List(railsStat, averageTrainWaitingStat, mostUsedTrains)

  def mostUsedRails(routes: List[Rail], n: Int = 3): MostUsedRails =
    MostUsedRails(mostUsedBy(routes, _.code))

  def averageTrainWaiting(trains: List[List[TrainPosition]]): AverageTrainWaiting =
    val waiting = trains.map(waitingTime).sum
    AverageTrainWaiting(waiting.toDouble / trains.size.toDouble)

  /** Computes the total waiting time of a train. */
  private def waitingTime(positions: List[TrainPosition]): Int =
    positions.foldLeft((OnRail(RailCode.empty), 0)) {
      case ((prevPos, wait), pos) =>
        pos match
          case AtStation(_) if pos == prevPos => (pos, wait + 1)
          case _ => (pos, wait)
    }._2

  def mostUsedTrain(itineraries: List[Itinerary]): MostUsedTrains =
    val trains = itineraries.flatMap(_.legs).map(_.train)
    MostUsedTrains(mostUsedBy(trains, _.code).map(_.code))

  private def mostUsedBy[A, K](items: List[A], key: A => K): List[A] =
    val usage = items.groupBy(key).map { case (_, group) => (group.head, group.size) }
    val maxUsage = usage.values.max
    usage.collect { case (elem, count) if count == maxUsage => elem }.toList
