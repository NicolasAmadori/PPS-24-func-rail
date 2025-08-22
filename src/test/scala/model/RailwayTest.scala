package model

import model.entities.Rail.{metalRail, titaniumRail}
import model.entities.Station.{bigStation, smallStation}

import model.railway.Railway
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class RailwayTest extends AnyFlatSpec:
  private val station1 = "ST001"
  private val station2 = "ST002"
  private val station3 = "ST003"

  "A Railway" can "be created empty" in {
    val emptyRailway = Railway.empty
    emptyRailway.stations should be(empty)
    emptyRailway.rails should be(empty)
  }

  it can "be created with stations and rails" in {
    val railway =
      Railway.withStations(List(smallStation(station1), bigStation(station2), smallStation(station3)))
        .withRails(List(
          metalRail(1, 200, station1, station2),
          metalRail(2, 500, station2, station3),
          titaniumRail(3, 300, station1, station3)
        ))
    railway.stations should not be empty
    railway.rails should not be empty
  }

  it can "be created with stations" in {
    val railway = Railway.withStations(List(smallStation(station1), bigStation(station2), smallStation(station3)))
    railway.stations should not be empty
    railway.rails should be(empty)
  }

  it can "be created with rails" in {
    val railway =
      Railway.withRails(List(
        metalRail(1, 200, station1, station2),
        metalRail(2, 500, station2, station3),
        titaniumRail(3, 300, station1, station3)
      ))
    railway.stations should be(empty)
    railway.rails should not be empty
  }
