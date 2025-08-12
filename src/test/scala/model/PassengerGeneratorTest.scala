package model

import model.entities.EntityCodes.StationCode
import model.entities.{Rail, Station}
import model.entities.Rail.metalRail
import model.entities.Station.{bigStation, smallStation}
import model.railway.Railway
import model.entities.PassengerState.AtStation
import model.util.PassengerGenerator
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PassengerGeneratorTest extends AnyFlatSpec with Matchers:

  // Stations for tests
  private val st1: Station = smallStation("ST1")
  private val st2: Station = smallStation("ST2")
  private val st3: Station = smallStation("ST3")
  private val bst1: Station = bigStation("BST1")

  // Rails for tests (bidirectional)
  private val rail1_2 = metalRail(1, 10, StationCode.value(st1.code), StationCode.value(st2.code))
  private val rail2_1 = metalRail(2, 10, StationCode.value(st2.code), StationCode.value(st1.code))
  private val rail2_3 = metalRail(3, 5, StationCode.value(st2.code), StationCode.value(st3.code))
  private val rail3_2 = metalRail(4, 5, StationCode.value(st3.code), StationCode.value(st2.code))
  private val rail_bst1_st1 = metalRail(5, 20, StationCode.value(bst1.code), StationCode.value(st1.code))
  private val rail_st1_bst1 = metalRail(6, 20, StationCode.value(st1.code), StationCode.value(bst1.code))

  it should "return an empty list if the railway has fewer than two stations" in {
    val railwayWithNoStations = Railway.empty
    val railwayWithOneStation = Railway.withStations(List(st1))

    val generator1 = new PassengerGenerator(railwayWithNoStations)
    val generator2 = new PassengerGenerator(railwayWithOneStation)

    generator1.generate() should be(empty)
    generator2.generate() should be(empty)
  }

  it should "generate a single passenger with correct properties on a simple railway" in {
    val simpleRailway = Railway.withStations(List(st1, st2)).withRails(List(rail1_2, rail2_1))
    val generator = new PassengerGenerator(simpleRailway)

    val result = generator.generate(1)
    result should have size 1

    val (passenger, state) = result.head

    passenger.id.toString should be("P1")

    // Departure and destination should be the two stations in the railway
    Set(passenger.departure, passenger.destination) should be(Set(st1.code, st2.code))
    passenger.departure should not be passenger.destination

    // The route should connect departure and destination
    passenger.route should be(defined)
    passenger.route.get.rails should have size 1
    passenger.route.get.rails.head.stationA should be(passenger.departure)
    passenger.route.get.rails.head.stationB should be(passenger.destination)

    // Initial state should be AtStation at the departure point
    state should be(AtStation(passenger.departure))
  }

  it should "generate multiple passengers with unique, sequential IDs" in {
    val simpleRailway = Railway.withStations(List(st1, st2)).withRails(List(rail1_2, rail2_1))
    val generator = new PassengerGenerator(simpleRailway)

    val results = generator.generate(3)
    results should have size 3

    val passengerIds = results.map(_._1.id.toString).sorted
    passengerIds should be(List("P1", "P2", "P3"))
  }

  it should "generate a passenger with no route if no path exists" in {
    // A railway with two stations but no rails connecting them
    val disconnectedRailway = Railway.withStations(List(st1, st2))
    val generator = new PassengerGenerator(disconnectedRailway)

    val result = generator.generate(1)
    result should have size 1

    val (passenger, _) = result.head
    passenger.route should be(None)
  }

  it should "find a valid route in a multi-station railway" in {
    val lineRailway = Railway.withStations(List(st1, st2, st3)).withRails(List(rail1_2, rail2_1, rail2_3, rail3_2))
    val generator = new PassengerGenerator(lineRailway)

    val passengers = generator.generate(5000).map(_._1)

    // Find a passenger with the specific route ST1 -> ST3
    val passenger_st1_st3 = passengers.find(p => p.departure == st1.code && p.destination == st3.code)

    // If such a passenger was generated, check its route
    passenger_st1_st3.foreach { p =>
      p.route should be(defined)
      p.route.get.rails should have size 2
      val stationSequence = p.route.get.rails.map(_.stationA) :+ p.route.get.rails.last.stationB
      stationSequence should be(List(st1.code, st2.code, st3.code))
    }
  }

  it should "prefer big stations as departure and arrival points" in {
    // Railway with one big station and one small station
    val mixedRailway = Railway.withStations(List(bst1, st1)).withRails(List(rail_bst1_st1, rail_st1_bst1))
    val generator = new PassengerGenerator(mixedRailway)
    val totalPassengers = 500

    val passengers = generator.generate(totalPassengers).map(_._1)

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
    val st1_st3 = metalRail(7, 8, StationCode.value(st1.code), StationCode.value(st3.code))
    val st3_st1 = metalRail(8, 8, StationCode.value(st3.code), StationCode.value(st1.code))
    val loopRailway =
      Railway.withStations(List(st1, st2, st3)).withRails(List(rail1_2, rail2_1, rail2_3, rail3_2, st1_st3, st3_st1))
    val generator = new PassengerGenerator(loopRailway)

    // All generated routes should be simple paths (no repeated nodes)
    val passengers = generator.generate(50).map(_._1)
    passengers.foreach { p =>
      p.route should be(defined)
      val routeStations = p.departure +: p.route.get.rails.map(_.stationB)
      val uniqueRouteStations = routeStations.distinct
      routeStations should be(uniqueRouteStations)
    }
  }
