package controller

import controller.simulation.util.*
import model.entities.EntityCodes.{RailCode, StationCode, TrainCode}
import model.entities.Rail.metalRail
import model.entities.*
import model.entities.dsl.buildNormalTrain
import model.entities.dsl.ItineraryDSL.leg
import model.entities.dsl.PassengerDSL.passenger
import model.simulation.PassengerState
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*
import util.SampleRailway
import util.SampleRailway.SampleStation.*

class StatisticProviderTest extends AnyFlatSpec:

  private val (stationA, stationB, stationC) = (StationCode(StationA), StationCode(StationB), StationCode(StationC))
  private val train1 = buildNormalTrain("T1"):
    _ departsFrom stationA stopsAt stationB
  private val train2 = buildNormalTrain("T2"):
    _ departsFrom stationB stopsAt stationC
  private val itinerary1 = Itinerary(List(
    leg(train1) from stationA to stationB,
    leg(train2) from stationB to stationC
  ))
  private val itinerary2 = Itinerary(List(
    leg(train1) from stationA to stationB
  ))

  "Statistic provider" should "retrieve most used rail" in {
    val rail1 = metalRail("MR1", 100, StationA, StationB)
    val rail2 = metalRail("MR2", 200, StationB, StationC)
    val rail3 = metalRail("MR3", 300, StationC, StationD)
    val rail4 = metalRail("MR4", 400, StationD, StationA)
    val route1 = Route(List(rail1, rail2, rail3, rail4, rail1))
    val route2 = Route(List(rail4, rail3, rail2, rail1))
    val ctx = SimulationContext(routes = List(route1, route2))

    val mostUsedRails = MostUsedRailsProvider.compute(ctx)
    mostUsedRails.rails.get should contain(RailCode("MR1"))
    mostUsedRails.unit should be("")
    mostUsedRails.toString should be("Most used rails")
  }

  it should "retrieve all most used rails if there's a tie" in {
    val rail1 = metalRail("MR1", 100, StationA, StationB)
    val rail2 = metalRail("MR2", 200, StationB, StationC)
    val rail3 = metalRail("MR3", 300, StationC, StationD)
    val rail4 = metalRail("MR4", 400, StationD, StationA)
    val route1 = Route(List(rail1, rail2, rail3))
    val route2 = Route(List(rail4, rail3, rail2, rail1))
    val ctx = SimulationContext(routes = List(route1, route2))

    val mostUsedRails = MostUsedRailsProvider.compute(ctx)
    mostUsedRails.rails.get should contain allOf (RailCode("MR1"), RailCode("MR2"), RailCode("MR3"))
    mostUsedRails.unit should be("")
    mostUsedRails.toString should be("Most used rails")
  }

  it should "retrieve average waiting time" in {
    import model.simulation.TrainPosition.*
    val positions1 = List(
      AtStation(StationCode(StationA)),
      OnRail(RailCode("MR1")),
      OnRail(RailCode("MR1")),
      OnRail(RailCode("MR1")),
      AtStation(StationCode(StationB)),
      AtStation(StationCode(StationB))
    )
    val positions2 = List(
      AtStation(StationCode(StationA)),
      AtStation(StationCode(StationA)),
      AtStation(StationCode(StationA)),
      OnRail(RailCode("MR1")),
      OnRail(RailCode("MR1")),
      OnRail(RailCode("MR1")),
      AtStation(StationCode(StationB))
    )
    val ctx = SimulationContext(trainHistories = List(positions1, positions2))

    val averageWaiting = AverageTrainWaitingProvider.compute(ctx)
    averageWaiting.hours.get should be(1.5)
    averageWaiting.unit should be("hours")
    averageWaiting.toString should be("Average train waiting")
  }

  it should "retrieve the most used train" in {
    val ctx = SimulationContext(itineraries = List(itinerary1, itinerary2))

    val mostUsedTrains = MostUsedTrainsProvider.compute(ctx)
    mostUsedTrains.trains.get should be(List(TrainCode("T1")))
  }

  it should "retrieve all the most used train if there's a tie" in {
    val ctx = SimulationContext(itineraries = List(itinerary1, itinerary1))

    val mostUsedTrains = MostUsedTrainsProvider.compute(ctx)
    mostUsedTrains.trains.get should contain allOf (TrainCode("T1"), TrainCode("T2"))
  }

  it should "retrieve incomplete trips count" in {
    val passenger1 = passenger("P1").from(stationA).to(stationC).withNoItinerary
    val passenger2 = passenger("P2").from(stationA).to(stationC).withItinerary(itinerary1)
    val passenger3 = passenger("P3").from(stationA).to(stationC).withNoItinerary
    val ctx = SimulationContext(passengers = List(passenger1, passenger2, passenger3))

    val incompleteTrips = IncompleteTripsProvider.compute(ctx)
    incompleteTrips.decimal.get should be(2.toDouble / 3)
  }

  it should "retrieve completed trips count" in {
    val passenger1 = passenger("P1").from(stationA).to(stationC).withItinerary(itinerary1)
    val passenger2 = passenger("P2").from(stationA).to(stationC).withItinerary(itinerary1)
    val passenger3 = passenger("P3").from(stationA).to(stationC).withNoItinerary
    val ctx = SimulationContext(passengers = List(passenger1, passenger2, passenger3))

    val completedTrips = CompletedTripsProvider.compute(ctx)
    completedTrips.decimal.get should be(2.toDouble / 3)
  }

  private def buildContextWithPassengersPositions: SimulationContext =
    import model.entities.PassengerPosition.*
    val positions1 = List(
      AtStation(stationA),
      OnTrain(TrainCode("T1")),
      OnTrain(TrainCode("T1")),
      OnTrain(TrainCode("T1")),
      AtStation(stationB),
      AtStation(stationB)
    )
    val positions2 = List(
      AtStation(stationA),
      AtStation(stationA),
      AtStation(stationA),
      OnTrain(TrainCode("T1")),
      OnTrain(TrainCode("T1")),
      OnTrain(TrainCode("T1")),
      AtStation(stationB)
    )
    val state1 = PassengerState(positions1.head, positions1)
    val state2 = PassengerState(positions1.head, positions2)
    SimulationContext(passengerStates = List(state1, state2), passengersWithCompletedTrip = List(state1, state2))

  it should "retrieve correct stations with most waiting" in {
    val ctx = buildContextWithPassengersPositions
    val stationsWithMostWaiting = StationsWithMostWaitingProvider.compute(ctx)
    stationsWithMostWaiting.stations.get should contain(stationA)
  }

  it should "retrieve correct average trip duration" in {
    val ctx = buildContextWithPassengersPositions

    val averageTripDuration = AverageTripDurationProvider.compute(ctx)
    averageTripDuration.hours.get should be(6.5)
  }

  it should "retrieve correct average passenger waiting" in {
    val ctx = buildContextWithPassengersPositions

    val averagePassengerWaiting = AveragePassengerWaitingProvider.compute(ctx)
    averagePassengerWaiting.hours.get should be(1.5)
  }

  it should "retrieve correct average passenger travel time" in {
    val ctx = buildContextWithPassengersPositions

    val averageTravelTime = AveragePassengerTravelTimeProvider.compute(ctx)
    averageTravelTime.hours.get should be(3)
  }
