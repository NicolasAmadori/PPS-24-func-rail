package model

import model.Rail.{emptyMetalRail, emptyTitaniumRail}
import model.Station.{bigStation, smallStation}
import model.Train.normalTrain
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class RailwayTest extends AnyFlatSpec:
  private val station1 = smallStation("ST001")
  private val station2 = bigStation("ST002")
  private val station3 = smallStation("ST003")
  private val railway = Railway.empty.withStations(List(station1, station2, station3))
    .withTrains(List(normalTrain(101)))
    .withRails(List(
      emptyMetalRail(1, 200, station1, station2),
      emptyMetalRail(2, 500, station2, station3),
      emptyTitaniumRail(3, 300, station1, station3)
    ))

  "A Railway" should "be created empty" in {
    val emptyRailway = Railway.empty
    emptyRailway.stations should be(empty)
    emptyRailway.trains should be(empty)
    emptyRailway.rails should be(empty)
  }

  it should "be created with stations, trains, and rails" in {
    railway.stations should not be empty
    railway.trains should not be empty
    railway.rails should not be empty
  }

  it should "allow adding a station" in {
    val newStation = smallStation("ST004")
    val updatedRailway = railway.addStation(newStation)
    updatedRailway.stations should contain(newStation)
  }

  it should "not add the same station twice" in {
    val updatedRailway = railway.addStation(station1)
    updatedRailway.stations.count(_ == station1) should be(1)
  }

  it should "allow adding a rail" in {
    val newRail = emptyMetalRail(4, 600, station1, station2)
    val updatedRailway = railway.addRail(newRail)
    updatedRailway.rails should contain(newRail)
  }

  it should "not add the same rail twice" in {
    val updatedRailway = railway.addRail(emptyMetalRail(1, 200, station1, station2))
    updatedRailway.rails.count(_ == emptyMetalRail(1, 200, station1, station2)) should be(1)
  }
