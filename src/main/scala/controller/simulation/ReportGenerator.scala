package controller.simulation

import model.entities.EntityCodes.RailCode
import model.entities.Rail
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

object ReportGenerator:
  import Statistic.*

  def createReport(simulation: Simulation): List[Statistic] =
    val railsStat = mostUsedRails(simulation.state.trains.flatMap(_.route.rails))
    val averageTrainWaitingStat =
      averageTrainWaiting(simulation.state.trainStates.values.map(_.previousPositions).toList)
    List(railsStat, averageTrainWaitingStat)

  def mostUsedRails(routes: List[Rail], n: Int = 3): MostUsedRails =
    val railUsage = routes.groupBy(_.code).map((_, l) => (l.head, l.size))
    val maxUsage = railUsage.values.max
    val mostUsed = railUsage.collect { case (rail, count) if count == maxUsage => rail }.toList
    MostUsedRails(mostUsed)

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
