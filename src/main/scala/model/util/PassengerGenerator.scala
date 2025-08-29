package model.util

import model.entities.EntityCodes.StationCode
import model.entities.PassengerPosition.AtStation
import model.entities.*
import model.entities.dsl.ItineraryDSL.leg
import model.entities.dsl.PassengerDSL
import model.entities.dsl.PassengerDSL.passenger
import model.railway.Railway
import model.simulation.PassengerState

import scala.util.Random

/** A utility class for generating passengers with randomized departure stations, arrival stations and routes.
  *
  * @param railway
  *   The railway system containing stations and rails.
  */
class PassengerGenerator(
    railway: Railway,
    trains: List[Train],
    itineraryStrategy: Int = 0,
    passengerIdCounter: Int = 0
):
  private val BIG_STATION_MULTIPLIER = 5

  /** Generates a specified number of passengers.
    *
    * Passengers are created with a starting station, a destination station, and a route to follow. The probability of a
    * passenger starting at a big station is higher.
    *
    * @param n
    *   The number of passengers to generate (defaults to 1).
    * @return
    *   A list of tuples, where each tuple contains a [[PassengerDSL]] and its initial [[PassengerState]].
    */
  def generate(n: Int = 1): (PassengerGenerator, List[(Passenger, PassengerState)], List[PassengerLog]) =
    if railway.stations.size < 2 then
      (this, List.empty, List.empty)
    else
      val randomizedStations: List[Station] =
        railway.stations.flatMap {
          case s @ SmallStation(_) => List(s)
          case s @ BigStation(_) => List.fill(BIG_STATION_MULTIPLIER)(s)
        }

      var counter = passengerIdCounter
      val passengers = (1 to n).map { _ =>
        val departureStation = Random.shuffle(randomizedStations).head
        val arrivalStation = Random.shuffle(randomizedStations.filter(_ != departureStation)).head
        val itinerary: Option[Itinerary] = itineraryStrategy match
          case 1 => getItineraryWithShortestTime(departureStation, arrivalStation)
          case 2 => getItineraryWithShortestDistance(departureStation, arrivalStation)
          case 3 => getItineraryWithLowestCost(departureStation, arrivalStation)
          case _ => getRandomItinerary(departureStation, arrivalStation)

        counter += 1
        val baseBuilder =
          passenger(s"P$counter")
            .from(departureStation.code)
            .to(arrivalStation.code)

        (
          itinerary.fold(baseBuilder.withNoItinerary)(baseBuilder.withItinerary),
          PassengerState(AtStation(departureStation.code))
        )
      }.toList

      (
        new PassengerGenerator(railway, trains, itineraryStrategy, counter),
        passengers,
        passengers.map(p => PassengerLog.StartTrip(p._1))
      )

  private def getRandomItinerary(departureStation: Station, arrivalStation: Station): Option[Itinerary] =
    Random.shuffle(findAllItineraries(departureStation.code, arrivalStation.code)).headOption

  private def getItineraryWithShortestTime(departureStation: Station, arrivalStation: Station): Option[Itinerary] =
    findAllItineraries(departureStation.code, arrivalStation.code).sortBy(i => i.totalTime).headOption

  private def getItineraryWithShortestDistance(departureStation: Station, arrivalStation: Station): Option[Itinerary] =
    findAllItineraries(departureStation.code, arrivalStation.code).sortBy(i => i.totalLength).headOption

  private def getItineraryWithLowestCost(departureStation: Station, arrivalStation: Station): Option[Itinerary] =
    findAllItineraries(departureStation.code, arrivalStation.code).sortBy(i => i.totalCost).headOption

  /** Return all the possible itineraries between two stations */
  private def findAllItineraries(
      start: StationCode,
      end: StationCode
  ): List[Itinerary] =
    dfs(current = start, target = end, visited = Set(start))

  /** DFS that explore all possible paths */
  private def dfs(
      current: StationCode,
      target: StationCode,
      visited: Set[StationCode],
      currentLegs: List[ItineraryLeg] = Nil
  ): List[Itinerary] =
    if current == target then
      List(Itinerary(currentLegs.reverse))
    else
      trains
        .filter(_.stations.contains(current))
        .flatMap { train =>
          val idx = train.stations.indexOf(current)
          val neighbors =
            train.stations.lift(idx - 1).toList ++
              train.stations.lift(idx + 1).toList

          neighbors
            .filterNot(visited.contains)
            .flatMap { next =>
              val newLegs =
                currentLegs match
                  case Nil =>
                    List(leg(train) from current to next)
                  case head :: tail if head.train == train =>
                    // Continue the same ItineraryLeg
                    (List(leg(train) from head.from to next)) ::: tail
                  case _ =>
                    // New ItineraryLeg with a different train
                    (leg(train) from current to next) :: currentLegs

              dfs(next, target, visited + next, newLegs)
            }
        }
