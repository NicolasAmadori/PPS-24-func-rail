package controller.simulation

import controller.simulation.Statistic.MostUsedRails
import model.entities.{Rail, Train}
import model.simulation.Simulation

trait Statistic:
  def getStringStat: String
  def getMeasurementUnit: String = ""

object Statistic:
  case class MostUsedRails(rails: List[Rail]) extends Statistic:
    override def toString: String = "Most used rails"
    def getStringStat: String = rails.mkString(" - ")

object ReportGenerator:
  def createReport(simulation: Simulation): List[Statistic] =
    val railsStat = mostUsedRails(simulation.state.trains)

    List(railsStat)

  private def mostUsedRails(trains: List[Train], n: Int = 3): MostUsedRails =
    MostUsedRails(trains.flatMap(_.route.rails)
      .groupBy(_.code)
      .map((_, l) => (l.head, l.size)).toList
      .sortBy((_, s) => -s).take(n)
      .map((r, _) => r))
