package controller.simulation.util

import controller.simulation.Statistic
import controller.simulation.Statistic.*
import model.entities.EntityCodes.StationCode
import model.entities.PassengerPosition
import model.simulation.TrainPosition

/** Trait to define a provider of statistic */
trait StatisticProvider:
  def compute(ctx: SimulationContext): Statistic

object MostUsedRailsProvider extends StatisticProvider:
  def compute(ctx: SimulationContext): MostUsedRails =
    val rails = ctx.routes.flatMap(_.rails)
    MostUsedRails(mostUsedBy(rails, _.code))

object AverageTrainWaitingProvider extends StatisticProvider:
  import TrainPosition.*

  override def compute(ctx: SimulationContext): AverageTrainWaiting =
    val trains = ctx.trainHistories
    val waiting = trains.map(waitingTime).sum
    AverageTrainWaiting(waiting / trains.size.toDouble)

  private def waitingTime(positions: List[TrainPosition]): Double =
    positions.sliding(2).count {
      case List(AtStation(st1), AtStation(st2)) if st1 == st2 => true
      case _ => false
    }.toDouble

object MostUsedTrainsProvider extends StatisticProvider:
  override def compute(ctx: SimulationContext): MostUsedTrains =
    val trains = ctx.itineraries.flatMap(_.legs).map(_.train)
    MostUsedTrains(mostUsedBy(trains, _.code).map(_.code))

object IncompleteTripsProvider extends StatisticProvider:
  override def compute(ctx: SimulationContext): IncompleteTrips =
    IncompleteTrips(ctx.passengers.count(_.itinerary.isEmpty))

object CompletedTripsProvider extends StatisticProvider:
  override def compute(ctx: SimulationContext): CompletedTrips =
    CompletedTrips(ctx.passengers.count(_.itinerary.nonEmpty))

object StationsWithMostWaitingProvider extends StatisticProvider:
  override def compute(ctx: SimulationContext): StationsWithMostWaiting =
    val positions = ctx.passengerStates.map(_.previousPositions).filter(_.nonEmpty).toList
    val waitingTimes = aggregateStationWaiting(positions)
    val maxWaiting = if waitingTimes.nonEmpty then waitingTimes.values.max else 0
    val stationsWithMaxWaiting = waitingTimes.collect { case (st, t) if t == maxWaiting => st }.toList
    StationsWithMostWaiting(stationsWithMaxWaiting)

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
