package controller.simulation

import model.entities.EntityCodes.StationCode
import model.entities.*
import model.simulation.TrainPosition.AtStation
import model.simulation.{Simulation, TrainPosition}

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
    val mostUsedTrains = mostUsedTrain(simulation.state.passengers.map(_.itinerary).collect { case Some(i) => i })
    val tripsCompleted = completedTrips(simulation.state.passengers)
    val impossibleTrips = incompleteTrips(simulation.state.passengers)
    val maxWaitingStations = stationsWithMostWaiting(simulation.state.passengerStates.values.toList)
    List(railsStat, averageTrainWaitingStat, mostUsedTrains, tripsCompleted, impossibleTrips, maxWaitingStations)

  /** Returns the list of the rails that appear the most in train routes */
  def mostUsedRails(routes: List[Route], n: Int = 3): MostUsedRails =
    val rails = routes.flatMap(_.rails)
    MostUsedRails(mostUsedBy(rails, _.code))

  /** Returns the average time a train waits in a station */
  def averageTrainWaiting(trains: List[List[TrainPosition]]): AverageTrainWaiting =
    val waiting = trains.map(waitingTime).sum
    AverageTrainWaiting(waiting / trains.size.toDouble)

  def mostUsedTrain(itineraries: List[Itinerary]): MostUsedTrains =
    val trains = itineraries.flatMap(_.legs).map(_.train)
    MostUsedTrains(mostUsedBy(trains, _.code).map(_.code))

  private def mostUsedBy[A, K](items: List[A], key: A => K): List[A] =
    if items.isEmpty then Nil
    else
      val usage = items.groupBy(key).map { case (_, group) => (group.head, group.size) }
      val maxUsage = usage.values.max
      usage.collect { case (elem, count) if count == maxUsage => elem }.toList

  /** @return the number of passengers that cannot complete their trip */
  def incompleteTrips(passengers: List[Passenger]): IncompleteTrips =
    IncompleteTrips(passengers.count(_.itinerary.isEmpty))

  /** @return the number of passengers that completed the trip */
  def completedTrips(passengers: List[Passenger]): CompletedTrips =
    CompletedTrips(passengers.count(_.itinerary.nonEmpty))

  /** @return the stations in which the passengers have to wait the most */
  def stationsWithMostWaiting(passengersState: List[PassengerState]): StationsWithMostWaiting =
    val positions = passengersState.map(_.previousPositions).filter(_.nonEmpty)
    val waitingTimes = aggregateStationWaiting(positions)
    val maxWaiting = if waitingTimes.nonEmpty then waitingTimes.values.max else 0
    val stationsWithMaxWaiting = waitingTimes.collect { case (st, t) if t == maxWaiting => st }.toList
    StationsWithMostWaiting(stationsWithMaxWaiting)

  /** Computes the total waiting time of a train */
  private def waitingTime(positions: List[TrainPosition]): Double =
    positions.sliding(2).count {
      case List(AtStation(st1), AtStation(st2)) if st1 == st2 => true
      case _ => false
    }.toDouble

  private def aggregateStationWaiting(histories: List[List[PassengerPosition]]): Map[StationCode, Int] =
    val perPassenger = histories.map { history =>
      history.collect { case PassengerPosition.AtStation(st) => st }
    }.map(waitingInItinerary)
    println(perPassenger)
    perPassenger.flatten
      .groupBy(_._1)
      .map { case (station, waits) => station -> (waits.map(_._2).sum / waits.size) }

  private def waitingInItinerary(stations: List[StationCode]): Map[StationCode, Int] =
    stations.sliding(2).collect {
      case List(st1, st2) if st1 == st2 => st1 -> 1
    }.toList.groupMapReduce(_._1)(_._2)(_ + _)
