package util

import model.entities.Rail.{metalRail, titaniumRail}
import model.entities.Station.{bigStation, smallStation}
import model.entities.{Rail, Station}
import model.railway.{Railway, RailwayImpl}

object SampleRailway:

  object SampleStation:
    val StationA = "Station A"
    val StationB = "Station B"
    val StationC = "Station C"
    val StationD = "Station D"
    val StationE = "Station E"

  import SampleStation.*

  /** Railway with only metal rails */
  def railway1: Railway =
    val stations = List(
      smallStation(StationA),
      bigStation(StationB),
      smallStation(StationC),
      bigStation(StationD),
      bigStation(StationE)
    )
    val rails = List(
      metalRail("MR1", 100, StationA, StationB),
      metalRail("MR2", 150, StationB, StationC),
      metalRail("MR3", 500, StationC, StationD),
      metalRail("MR4", 120, StationD, StationA),
      metalRail("MR5", 120, StationD, StationE)
    )
    RailwayImpl(stations, rails)

  /** Railway with both metal and titanium rails so that some stations cannot be reached without crossing titanium rail
    */
  def railway2: Railway =
    val stations = List(
      smallStation(StationA),
      bigStation(StationB),
      smallStation(StationC),
      bigStation(StationD),
      bigStation(StationE)
    )
    val rails = List(
      metalRail("MR1", 100, StationA, StationB),
      titaniumRail("TR2", 150, StationB, StationC),
      metalRail("MR3", 200, StationC, StationD),
      titaniumRail("TR4", 120, StationD, StationA),
      metalRail("MR5", 120, StationD, StationE)
    )
    RailwayImpl(stations, rails)

  /** Railway with only two stations and a metal rail */
  def railway3: Railway =
    val stations = List(
      smallStation(StationA),
      bigStation(StationB)
    )
    val rails = List(
      metalRail("MR1", 1000, StationA, StationB)
    )
    RailwayImpl(stations, rails)

  /** Railway with no route for normal trains */
  def railway4: Railway =
    val stations = List(
      smallStation(StationA),
      bigStation(StationB)
    )
    val rails = List(
      titaniumRail("TR1", 1000, StationA, StationB)
    )
    RailwayImpl(stations, rails)

  /** Small railway with three stations and two metal rails */
  def railway5: Railway =
    val stations = List(
      smallStation(StationA),
      bigStation(StationB),
      smallStation(StationC)
    )
    val rails = List(
      metalRail("MR1", 300, StationA, StationB),
      metalRail("MR2", 200, StationB, StationC)
    )
    RailwayImpl(stations, rails)

  /** Small railway with multiple rails connecting two stations */
  def railway6: Railway =
    val stations = List(
      smallStation(StationA),
      smallStation(StationB)
    )
    val rails = List(
      metalRail("MR1", 100, StationA, StationB),
      metalRail("MR2", 200, StationA, StationB),
      titaniumRail("TR1", 300, StationA, StationB)
    )
    RailwayImpl(stations, rails)
