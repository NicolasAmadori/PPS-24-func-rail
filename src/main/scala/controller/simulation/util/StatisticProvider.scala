package controller.simulation.util

import Statistic.*
import model.entities.EntityCodes.StationCode
import model.entities.PassengerPosition
import model.entities.PassengerPosition.{AtStation, OnTrain}
import model.simulation.TrainPosition

/** Trait to define a provider of statistic */
trait StatisticProvider:
  def compute(ctx: SimulationContext): Statistic

object MostUsedRailsProvider extends StatisticProvider:
  def compute(ctx: SimulationContext): MostUsedRails =
    val rails = ctx.routes.flatMap(_.rails)
    val mostUsed = mostUsedBy(rails, _.code).map(_.code)
    if mostUsed.isEmpty then
      MostUsedRails(None)
    else
      MostUsedRails(Option(mostUsed))

object AverageTrainWaitingProvider extends StatisticProvider:
  def compute(ctx: SimulationContext): AverageTrainWaiting =
    val trains = ctx.trainHistories
    if trains.isEmpty then AverageTrainWaiting(None)
    else
      val waiting = trains.map(consecutiveSameBy(_) {
        case TrainPosition.AtStation(st) => Some(st)
        case _ => None
      }).sum
      AverageTrainWaiting(Some(waiting / trains.size.toDouble))

object MostUsedTrainsProvider extends StatisticProvider:
  def compute(ctx: SimulationContext): MostUsedTrains =
    val trains = ctx.itineraries.flatMap(_.legs).map(_.train)
    val mostUsed = mostUsedBy(trains, _.code).map(_.code)
    if mostUsed.isEmpty then
      MostUsedTrains(None)
    else
      MostUsedTrains(Option(mostUsed))

object IncompleteTripsProvider extends StatisticProvider:
  def compute(ctx: SimulationContext): IncompleteTrips =
    IncompleteTrips(Option(ctx.passengers.count(_.itinerary.isEmpty)))

object CompletedTripsProvider extends StatisticProvider:
  def compute(ctx: SimulationContext): CompletedTrips =
    CompletedTrips(Option(ctx.passengers.count(_.itinerary.nonEmpty)))

object StationsWithMostWaitingProvider extends StatisticProvider:
  def compute(ctx: SimulationContext): StationsWithMostWaiting =
    val positions = ctx.passengerStates.map(_.previousPositions).filter(_.nonEmpty)
    val waitingTimes = aggregateStationWaiting(positions)
    val maxWaiting = if waitingTimes.nonEmpty then waitingTimes.values.max else 0
    val stationsWithMaxWaiting = waitingTimes.collect { case (st, t) if t == maxWaiting => st }.toList
    StationsWithMostWaiting(Option(stationsWithMaxWaiting))

  private def aggregateStationWaiting(histories: List[List[PassengerPosition]]): Map[StationCode, Int] =
    histories.flatMap { history =>
      history.collect { case PassengerPosition.AtStation(st) => st }
    }.groupBy(c => c).map { case (c, group) => (c, group.size) }

object AverageTripDurationProvider extends StatisticProvider:
  def compute(ctx: SimulationContext): AverageTripDuration =
    if ctx.passengersWithCompletedTrip.isEmpty then AverageTripDuration(None)
    else
      val averageDuration =
        ctx.passengersWithCompletedTrip.map(_.previousPositions.size).sum.toDouble / ctx.passengerStates.size.toDouble
      AverageTripDuration(Option(averageDuration))

object AveragePassengerWaitingProvider extends StatisticProvider:
  def compute(ctx: SimulationContext): AveragePassengerWaiting =
    val passengersPositions = ctx.passengersWithCompletedTrip.map(_.previousPositions)
    if passengersPositions.isEmpty then AveragePassengerWaiting(None)
    else
      val waiting = passengersPositions.map(Statistic.consecutiveSameBy(_) {
        case PassengerPosition.AtStation(st) => Some(st)
        case _ => None
      }).sum
      AveragePassengerWaiting(Option(waiting / passengersPositions.size.toDouble))

object AveragePassengerTravelTimeProvider extends StatisticProvider:
  def compute(ctx: SimulationContext): AveragePassengerTravelTime =
    val passengerPosition = ctx.passengersWithCompletedTrip.map(_.previousPositions)
    if passengerPosition.isEmpty then AveragePassengerTravelTime(None)
    else
      val travelTime = passengerPosition.flatten.collect { case OnTrain(t) => t }.size
      AveragePassengerTravelTime(Option(travelTime / passengerPosition.size.toDouble))
