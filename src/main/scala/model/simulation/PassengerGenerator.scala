package model.simulation

import model.railway.{BigStation, Rail, Railway, SmallStation, Station}
import model.simulation.Domain.PassengerCode
import model.simulation.PassengerState.AtStation

import scala.List
import scala.util.Random

class PassengerGenerator(railway: Railway):
  private val BIG_STATION_MULTIPLIER = 9

  private var passengerIdCounter = 0

  def generate(n: Int = 1): List[(Passenger, PassengerState)] =
    (1 to n).map { _ =>
      val randomizedStations: List[Station] = railway.stations.flatMap(s =>
        s match
          case SmallStation => List(s)
          case BigStation =>
            List.fill(BIG_STATION_MULTIPLIER)(s) // Big stations has 9 times the possibilities of being chosen
      )

      val starting = Random.shuffle(randomizedStations).head
      val destination = Random.shuffle(randomizedStations.filter(s => s != starting)).head

      passengerIdCounter += 1
      (
        PassengerImpl(
          PassengerCode(s"P-${passengerIdCounter}"),
          starting.code,
          destination.code,
          getRandomRoute(starting, destination)
        ),
        AtStation(starting.code)
      )
    }.toList

  private def getRandomRoute(startingStation: Station, destinationStation: Station): List[Rail] =
    Random.shuffle(getAllRoutes(startingStation, destinationStation)).head

  private def getAllRoutes(
      startingStation: Station,
      destinationStation: Station,
      route: List[Rail] = List.empty,
      usedRails: Set[Rail] = Set.empty
  ): List[List[Rail]] =
    if startingStation.code == destinationStation.code then
      List(route)
    else
      val connectedStations: List[Rail] = railway.rails.filter(r => r.stationA == startingStation)
      connectedStations.filterNot(usedRails.contains).flatMap { r =>
        val neighbor = railway.stations.find(_.code == r.stationB).get
        val newUsedRails = usedRails + r
        getAllRoutes(neighbor, destinationStation, route :+ r, newUsedRails)
      }
