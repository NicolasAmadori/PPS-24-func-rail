package utils

import model.railway.Rail.{metalRail, titaniumRail}
import model.railway.Station.{bigStation, smallStation}
import model.railway.{BigStation, MetalRail, Rail, Railway, RailwayImpl, SmallStation, Station}

object SampleRailway:

  object SampleStation:
    val StationA = "Station A"
    val StationB = "Station B"
    val StationC = "Station C"
    val StationD = "Station D"
    val StationE = "Station E"

  import SampleStation.*
  
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
