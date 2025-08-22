package model

import model.entities.EntityCodes.StationCode
import model.entities.{Rail, Station}
import model.entities.Rail.metalRail
import model.entities.Station.{bigStation, smallStation}
import model.railway.Railway
import model.entities.PassengerPosition.AtStation
import model.entities.Train.normalTrain
import model.simulation.RouteHelper
import model.util.PassengerGenerator
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class PassengerGeneratorTest extends AnyFlatSpec with Matchers:

  // Stations for tests
  private val st1: Station = smallStation("ST1")
  private val st2: Station = smallStation("ST2")
  private val st3: Station = smallStation("ST3")
  private val bst1: Station = bigStation("BST1")

  // Rails for tests (bidirectional)
  private val rail1_2 = metalRail("MR1", 10, StationCode.value(st1.code), StationCode.value(st2.code))
  private val rail2_1 = metalRail("MR2", 10, StationCode.value(st2.code), StationCode.value(st1.code))
  private val rail2_3 = metalRail("MR3", 5, StationCode.value(st2.code), StationCode.value(st3.code))
  private val rail3_2 = metalRail("MR4", 5, StationCode.value(st3.code), StationCode.value(st2.code))
  private val rail_bst1_st1 = metalRail("MR5", 20, StationCode.value(bst1.code), StationCode.value(st1.code))
  private val rail_st1_bst1 = metalRail("MR6", 20, StationCode.value(st1.code), StationCode.value(bst1.code))

  // Train for tests
  private val t1 = normalTrain("T1", List(st1.code, st2.code))
  private val t2 = normalTrain("T2", List(st1.code, st2.code, st3.code))

  it should "return an empty list if the railway has fewer than two stations" in {
    val railwayWithNoStations = Railway.empty
    val railwayWithOneStation = Railway.withStations(List(st1))

    val generator1 = new PassengerGenerator(railwayWithNoStations, List.empty)
    val generator2 = new PassengerGenerator(railwayWithOneStation, List.empty)

    val (_, passengers1, _) = generator1.generate()
    val (_, passengers2, _) = generator2.generate()

    passengers1 should be(empty)
    passengers2 should be(empty)
  }

  it should "generate a single passenger with correct properties on a simple railway" in {
    val simpleRailway = Railway.withStations(List(st1, st2)).withRails(List(rail1_2, rail2_1))
    val t1_copy = t1.withRoute(RouteHelper.getRouteForTrain(t1, simpleRailway).get)
    val generator = new PassengerGenerator(simpleRailway, List(t1_copy))

    val (_, result, _) = generator.generate(1)
    result should have size 1

    val (passenger, state) = result.head

    passenger.code.toString should be("P1")

    // Departure and destination should be the two stations in the railway
    Set(passenger.departure, passenger.destination) should be(Set(st1.code, st2.code))
    passenger.departure should not be passenger.destination

    // The itinerary should connect departure and destination
    passenger.itinerary shouldBe defined
    passenger.itinerary.get.totalLength should be(10.0)
    Set(passenger.itinerary.get.start, passenger.itinerary.get.end) should be(Set(st1.code, st2.code))

    // Initial state should be AtStation at the departure point
    state.currentPosition should be(AtStation(passenger.departure))
  }

  it should "generate multiple passengers with unique, sequential IDs" in {
    val simpleRailway = Railway.withStations(List(st1, st2)).withRails(List(rail1_2, rail2_1))
    val generator = new PassengerGenerator(simpleRailway, List.empty)

    val (_, results, _) = generator.generate(3)
    results should have size 3

    val passengerIds = results.map(_._1.code.toString).sorted
    passengerIds should be(List("P1", "P2", "P3"))
  }

  it should "generate a passenger with no itinerary if no train exists" in {
    // A railway with two stations but no rails connecting them
    val disconnectedRailway = Railway.withStations(List(st1, st2))
    val generator = new PassengerGenerator(disconnectedRailway, List.empty)

    val (_, result, _) = generator.generate(1)
    result should have size 1

    val (passenger, _) = result.head
    passenger.itinerary shouldBe None
  }

  it should "find a valid itinerary in a multi-station railway" in {
    val lineRailway = Railway.withStations(List(st1, st2, st3)).withRails(List(rail1_2, rail2_1, rail2_3, rail3_2))
    val t2_copy = t2.withRoute(RouteHelper.getRouteForTrain(t2, lineRailway).get)
    val generator = new PassengerGenerator(lineRailway, List(t2_copy))

    val passengers = generator.generate(5000)._2.map(_._1)

    // Find a passenger with the specific route ST1 -> ST3
    val passenger_st1_st3 = passengers.find(p => p.departure == st1.code && p.destination == st3.code)

    // If such a passenger was generated, check its route
    passenger_st1_st3.foreach { p =>
      p.itinerary should be(defined)
      p.itinerary.get.totalLength should be(15.0)
      p.itinerary.get.stations should be(List(st1.code, st2.code, st3.code))
    }
  }

  it should "prefer big stations as departure and arrival points" in {
    // Railway with one big station and one small station
    val mixedRailway = Railway.withStations(List(bst1, st1)).withRails(List(rail_bst1_st1, rail_st1_bst1))
    val generator = new PassengerGenerator(mixedRailway, List.empty)
    val totalPassengers = 500

    val passengers = generator.generate(totalPassengers)._2.map(_._1)

    // Count departures from each station type
    val departureCounts = passengers.groupMapReduce(_.departure)(_ => 1)(_ + _)
    val bigStationDepartures = departureCounts.getOrElse(bst1.code, 0)
    val smallStationDepartures = departureCounts.getOrElse(st1.code, 0)

    // Big stations are 9 times more likely to be chosen.
    bigStationDepartures should be > smallStationDepartures
    bigStationDepartures should be > (totalPassengers * 0.8).toInt // Allow some variance
    smallStationDepartures should be < (totalPassengers * 0.2).toInt // Allow some variance
  }

  it should "not create a route that visits the same station twice" in {
    // Railway with a loop: ST1 -> ST2 -> ST3 -> ST1
    val st1_st3 = metalRail("MR7", 8, StationCode.value(st1.code), StationCode.value(st3.code))
    val st3_st1 = metalRail("MR8", 8, StationCode.value(st3.code), StationCode.value(st1.code))
    val loopRailway =
      Railway.withStations(List(st1, st2, st3)).withRails(List(rail1_2, rail2_1, rail2_3, rail3_2, st1_st3, st3_st1))
    val t2_copy = t2.withRoute(RouteHelper.getRouteForTrain(t2, loopRailway).get)
    val generator = new PassengerGenerator(loopRailway, List(t2_copy))

    // All generated routes should be simple paths (no repeated nodes)
    val passengers = generator.generate(50)._2.map(_._1)
    passengers.foreach { p =>
      p.itinerary should be(defined)
      val itineraryStations = p.itinerary.get.stations
      val uniqueItineraryStations = itineraryStations.distinct
      itineraryStations should be(uniqueItineraryStations)
    }
  }
