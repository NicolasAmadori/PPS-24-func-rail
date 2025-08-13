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
      metalRail(1, 100, StationA, StationB),
      metalRail(2, 150, StationB, StationC),
      metalRail(3, 200, StationC, StationD),
      metalRail(4, 120, StationD, StationA),
      metalRail(5, 120, StationD, StationE)
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
      metalRail(1, 100, StationA, StationB),
      titaniumRail(2, 150, StationB, StationC),
      metalRail(3, 200, StationC, StationD),
      titaniumRail(4, 120, StationD, StationA),
      metalRail(5, 120, StationD, StationE)
    )
    RailwayImpl(stations, rails)

  /** Railway with only metal rail and longer lengths */
  def railway3: Railway =
    val stations = List(
      smallStation(StationA),
      bigStation(StationB),
      smallStation(StationC),
      bigStation(StationD),
      bigStation(StationE)
    )
    val rails = List(
      metalRail(1, 1000, StationA, StationB),
      metalRail(2, 1500, StationB, StationC),
      metalRail(3, 2000, StationC, StationD),
      metalRail(4, 1200, StationD, StationA),
      metalRail(5, 1200, StationD, StationE)
    )
    RailwayImpl(stations, rails)
