package controller.simulation

import model.entities.EntityCodes.RailCode
import model.entities.{Rail, Train}
import model.simulation.TrainPosition.{AtStation, OnRail}
import model.simulation.{Simulation, TrainPosition, TrainState}

trait Statistic:
  def getStringStat: String
  def getMeasurementUnit: String = ""

object Statistic:
  case class MostUsedRails(rails: List[Rail]) extends Statistic:
    override def toString: String = "Most used rails"
    def getStringStat: String = rails.mkString(" - ")
  case class AverageTrainWaiting(time: Double) extends Statistic:
    override def toString: String = "Average train waiting"
    def getStringStat: String = (if time / 24 < 1 then time else (time / 24)).toString
    override def getMeasurementUnit: String = if time / 24 < 1 then "hours" else "days"

object ReportGenerator:
  import Statistic.*

  def createReport(simulation: Simulation): List[Statistic] =
    val railsStat = mostUsedRails(simulation.state.trains)
    val averageTrainWaitingStat = averageTrainWaiting(simulation.state.trainStates.values.toList)
    println("TRAIN WAIT! " + averageTrainWaitingStat.time + " is " + averageTrainWaitingStat.getStringStat)
    List(railsStat, averageTrainWaitingStat)

  private def mostUsedRails(trains: List[Train], n: Int = 3): MostUsedRails =
    MostUsedRails(trains.flatMap(_.route.rails)
      .groupBy(_.code)
      .map((_, l) => (l.head, l.size)).toList
      .sortBy((_, s) => -s).take(n)
      .map((r, _) => r))

  private def averageTrainWaiting(trains: List[TrainState]): AverageTrainWaiting =
    val waiting = trains.map(t =>
      t.previousPositions.foldLeft((OnRail(RailCode.empty), 0)) { (a, p) =>
        val (prevPos, wait) = a
        p match
          case AtStation(_) => if p == prevPos then (p, wait + 1) else (p, wait)
          case _ => (p, wait)
      }._2
    ).sum()
    AverageTrainWaiting(waiting / trains.size)
