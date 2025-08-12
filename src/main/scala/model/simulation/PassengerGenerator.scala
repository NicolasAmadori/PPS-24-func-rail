package model.simulation

import model.railway.EntityCodes.StationCode
import model.railway.{BigStation, Rail, Railway, SmallStation, Station}
import model.simulation.Domain.PassengerCode
import model.simulation.PassengerState.AtStation

import scala.util.Random

/** A utility class for generating passengers with randomized departure stations, arrival stations and routes.
  *
  * @param railway
  *   The railway system containing stations and rails.
  */
class PassengerGenerator(railway: Railway):
  private val BIG_STATION_MULTIPLIER = 9

  private var passengerIdCounter = 0

  /** Generates a specified number of passengers.
    *
    * Passengers are created with a starting station, a destination station, and a route to follow. The probability of a
    * passenger starting at a big station is higher.
    *
    * @param n
    *   The number of passengers to generate (defaults to 1).
    * @return
    *   A list of tuples, where each tuple contains a [[Passenger]] and its initial [[PassengerState]].
    */
  def generate(n: Int = 1): List[(Passenger, PassengerState)] =
    if railway.stations.size < 2 then
      List.empty
    else
      (1 to n).map { _ =>
        val randomizedStations: List[Station] = railway.stations.flatMap(s =>
          s match
            case SmallStation(_) => List(s)
            case BigStation(_) =>
              List.fill(BIG_STATION_MULTIPLIER)(s) // Big stations has 9 times the possibilities of being chosen
        )

        val departureStation = Random.shuffle(randomizedStations).head
        val arrivalStation = Random.shuffle(randomizedStations.filter(s => s != departureStation)).head
        passengerIdCounter += 1
        (
          PassengerImpl(
            PassengerCode(s"P${passengerIdCounter}"),
            departureStation.code,
            arrivalStation.code,
            getRandomRoute(departureStation, arrivalStation)
          ),
          AtStation(departureStation.code)
        )
      }.toList

  /** Selects a random route from all available routes between two given stations.
    *
    * @param departureStation
    *   The station where the route begins.
    * @param arrivalStation
    *   The station where the route ends.
    * @return
    *   The randomly selected route if one is available, None otherwise.
    */
  private def getRandomRoute(departureStation: Station, arrivalStation: Station): Option[Route] =
    Random.shuffle(getAllRoutes(departureStation, arrivalStation)).headOption

  /** Recursively finds all possible routes between two stations.
    *
    * This is a depth-first search (DFS) algorithm that explores all paths from the departure station to the arrival
    * one.
    *
    * @param departureStation
    *   The current station in the search.
    * @param arrivalStation
    *   The final destination station.
    * @return
    *   A list of all possible routes.
    */
  private def getAllRoutes(
      departureStation: Station,
      arrivalStation: Station
  ): List[Route] =

    def _getAllRoutes(
        departureStation: Station,
        arrivalStation: Station,
        visitedNodes: Set[StationCode] = Set.empty,
        route: List[Rail] = List.empty,
        usedRails: Set[Rail] = Set.empty
    ): List[Route] =
      if departureStation.code == arrivalStation.code then
        List(Route(route))
      else
        val outgoingRails: List[Rail] = railway.rails.filter(r => r.stationA == departureStation.code)
        outgoingRails
          .filterNot(usedRails.contains)
          .filterNot(r => visitedNodes.contains(r.stationB))
          .flatMap { r =>
            val neighbor = railway.stations.find(_.code == r.stationB).get
            val newUsedRails = usedRails ++ railway.rails.filter(r2 => r2.code == r.code)
            _getAllRoutes(neighbor, arrivalStation, visitedNodes + r.stationB, route :+ r, newUsedRails)
          }

    _getAllRoutes(departureStation, arrivalStation, Set(departureStation.code))
