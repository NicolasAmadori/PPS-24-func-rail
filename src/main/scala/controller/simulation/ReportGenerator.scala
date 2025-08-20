package controller.simulation

import model.entities.EntityCodes.{RailCode, TrainCode}
import model.entities.{Itinerary, Passenger, Rail, Route}
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
  case class IncompleteTrips(count: Int) extends Statistic:
    override def toString: String = "Passengers who can't complete the trip"
    def getStringStat: String = count.toString
  case class CompletedTrips(count: Int) extends Statistic:
    override def toString: String = "Passengers who can arrive at destination"
    def getStringStat: String = count.toString

object ReportGenerator:
  import Statistic.*

  /** Creates a report of a simulation instance returning the list of statistics.
    * @param simulation
    *   the simulation to analyze
    * @return
    *   the list of statistics
    */
  def createReport(simulation: Simulation): List[Statistic] =
    val railsStat = mostUsedRails(simulation.state.trains.map(_.route))
    val averageTrainWaitingStat =
      averageTrainWaiting(simulation.state.trainStates.values.map(_.previousPositions).toList)
    val mostUsedTrains = mostUsedTrain(simulation.state.passengers.map(_.itinerary.get))
    val tripsCompleted = completedTrips(simulation.state.passengers)
    val impossibleTrips = incompleteTrips(simulation.state.passengers)
    List(railsStat, averageTrainWaitingStat, mostUsedTrains, tripsCompleted, impossibleTrips)

  /** Returns the list of the rails that appear the most in train routes */
  def mostUsedRails(routes: List[Route], n: Int = 3): MostUsedRails =
    val rails = routes.flatMap(_.rails)
    MostUsedRails(mostUsedBy(rails, _.code))

  /** Returns the average time a train waits in a station */
  def averageTrainWaiting(trains: List[List[TrainPosition]]): AverageTrainWaiting =
    val waiting = trains.map(waitingTime).sum
    AverageTrainWaiting(waiting / trains.size.toDouble)

  /** Computes the total waiting time of a train */
  private def waitingTime(positions: List[TrainPosition]): Double =
    positions.foldLeft((OnRail(RailCode.empty), 0)) {
      case ((prevPos, wait), pos) =>
        pos match
          case AtStation(_) if pos == prevPos => (pos, wait + 1)
          case _ => (pos, wait)
    }._2.toDouble

  def mostUsedTrain(itineraries: List[Itinerary]): MostUsedTrains =
    val trains = itineraries.flatMap(_.legs).map(_.train)
    MostUsedTrains(mostUsedBy(trains, _.code).map(_.code))

  private def mostUsedBy[A, K](items: List[A], key: A => K): List[A] =
    val usage = items.groupBy(key).map { case (_, group) => (group.head, group.size) }
    val maxUsage = usage.values.max
    usage.collect { case (elem, count) if count == maxUsage => elem }.toList

  def incompleteTrips(passengers: List[Passenger]): IncompleteTrips =
    IncompleteTrips(passengers.count(_.itinerary.isEmpty))

  def completedTrips(passengers: List[Passenger]): CompletedTrips =
    CompletedTrips(passengers.count(_.itinerary.nonEmpty))
