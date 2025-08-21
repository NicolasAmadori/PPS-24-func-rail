package controller.simulation.util

import controller.simulation.Statistic
import controller.simulation.Statistic.*
import model.entities.EntityCodes.StationCode
import model.entities.PassengerPosition
import model.entities.PassengerPosition.OnTrain
import model.simulation.TrainPosition

/** Trait to define a provider of statistic */
trait StatisticProvider:
  def compute(ctx: SimulationContext): Statistic

object MostUsedRailsProvider extends StatisticProvider:
  def compute(ctx: SimulationContext): MostUsedRails =
    val rails = ctx.routes.flatMap(_.rails)
    MostUsedRails(mostUsedBy(rails, _.code))

object AverageTrainWaitingProvider extends StatisticProvider:
  def compute(ctx: SimulationContext): AverageTrainWaiting =
    val trains = ctx.trainHistories
    val waiting = trains.map(consecutiveSameBy(_) {
      case TrainPosition.AtStation(st) => Some(st)
      case _ => None
    }).sum
    AverageTrainWaiting(waiting / trains.size.toDouble)

object MostUsedTrainsProvider extends StatisticProvider:
  def compute(ctx: SimulationContext): MostUsedTrains =
    val trains = ctx.itineraries.flatMap(_.legs).map(_.train)
    MostUsedTrains(mostUsedBy(trains, _.code).map(_.code))

object IncompleteTripsProvider extends StatisticProvider:
  def compute(ctx: SimulationContext): IncompleteTrips =
    IncompleteTrips(ctx.passengers.count(_.itinerary.isEmpty))

object CompletedTripsProvider extends StatisticProvider:
  def compute(ctx: SimulationContext): CompletedTrips =
    CompletedTrips(ctx.passengers.count(_.itinerary.nonEmpty))

object StationsWithMostWaitingProvider extends StatisticProvider:
  def compute(ctx: SimulationContext): StationsWithMostWaiting =
    val positions = ctx.passengerStates.map(_.previousPositions).filter(_.nonEmpty).toList
    val waitingTimes = aggregateStationWaiting(positions)
    val maxWaiting = if waitingTimes.nonEmpty then waitingTimes.values.max else 0
    val stationsWithMaxWaiting = waitingTimes.collect { case (st, t) if t == maxWaiting => st }.toList
    StationsWithMostWaiting(stationsWithMaxWaiting)

  private def aggregateStationWaiting(histories: List[List[PassengerPosition]]): Map[StationCode, Int] =
    val perPassenger = histories.map { history =>
      history.collect { case PassengerPosition.AtStation(st) => st }
    }.map(waitingInItinerary)
    perPassenger.flatten
      .groupBy(_._1)
      .map { case (station, waits) => station -> (waits.map(_._2).sum / waits.size) }

  private def waitingInItinerary(stations: List[StationCode]): Map[StationCode, Int] =
    stations.sliding(2).collect {
      case List(st1, st2) if st1 == st2 => st1 -> 1
    }.toList.groupMapReduce(_._1)(_._2)(_ + _)

object AverageTripDurationProvider extends StatisticProvider:
  def compute(ctx: SimulationContext): AverageTripDuration =
    val averageDuration =
      ctx.passengerStates.map(_.previousPositions.size).sum.toDouble / ctx.passengerStates.size.toDouble
    AverageTripDuration(averageDuration)

object AveragePassengerWaitingProvider extends StatisticProvider:
  def compute(ctx: SimulationContext): AveragePassengerWaiting =
    val passengersPositions = ctx.passengerStates.map(_.previousPositions)
    val waiting = passengersPositions.map(Statistic.consecutiveSameBy(_) {
      case PassengerPosition.AtStation(st) => Some(st)
      case _ => None
    }).sum
    AveragePassengerWaiting(waiting / passengersPositions.size.toDouble)

object AveragePassengerTravelTimeProvider extends StatisticProvider:
  def compute(ctx: SimulationContext): AveragePassengerTravelTime =
    val passengerPosition = ctx.passengerStates.map(_.previousPositions)
    val travelTime = passengerPosition.flatten.collect { case OnTrain(t) => t}.size
    AveragePassengerTravelTime(travelTime / passengerPosition.size.toDouble)