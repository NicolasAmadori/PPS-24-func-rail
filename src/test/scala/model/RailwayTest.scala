package model

import model.Rail.{metalRail, titaniumRail}
import model.Station.{bigStation, smallStation}
import model.Train.normalTrain
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class RailwayTest extends AnyFlatSpec:
  private val station1 = "ST001"
  private val station2 = "ST002"
  private val station3 = "ST003"
  private val railway =
    Railway.empty.withStations(List(smallStation(station1), bigStation(station2), smallStation(station3)))
      .withTrains(List(normalTrain(101)))
      .withRails(List(
        metalRail(1, 200, station1, station2),
        metalRail(2, 500, station2, station3),
        titaniumRail(3, 300, station1, station3)
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
    val updatedRailway = railway.addStation(smallStation(station1))
    updatedRailway.stations.count(_.code.equals(station1)) should be(1)
  }

  it should "allow adding a rail" in {
    val newRail = metalRail(4, 600, station1, station2)
    val updatedRailway = railway.addRail(newRail)
    updatedRailway.rails should contain(newRail)
  }

  it should "not add the same rail twice" in {
    val updatedRailway = railway.addRail(metalRail(1, 200, station1, station2))
    updatedRailway.rails.count(_ == metalRail(1, 200, station1, station2)) should be(1)
  }
