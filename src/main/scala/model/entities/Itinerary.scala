package model.entities

import model.entities.EntityCodes.StationCode

/** Domain-specific language (DSL) for building itinerary legs.
  */
object ItineraryDSL:

  /** Starts building an itinerary leg using the given train.
    *
    * @param train
    *   the train used for the leg
    * @return
    *   a builder to specify the departure station
    */
  def leg(train: Train): ItineraryLegBuilderFrom =
    ItineraryLegBuilderFrom(train)

  /** Builder to define the departure station of an itinerary leg.
    *
    * @param train
    *   the train used in the leg
    */
  final case class ItineraryLegBuilderFrom(train: Train):
    /** Specifies the departure station of the leg.
      *
      * @param station
      *   the departure station
      * @return
      *   a builder to specify the arrival station
      */
    infix def from(station: StationCode): ItineraryLegBuilderTo =
      ItineraryLegBuilderTo(train, station)

  /** Builder to define the arrival station of an itinerary leg.
    *
    * @param train
    *   the train used in the leg
    * @param from
    *   the already defined departure station
    */
  final case class ItineraryLegBuilderTo(train: Train, from: StationCode):
    /** Specifies the arrival station and builds the final itinerary leg.
      *
      * @param to
      *   the arrival station
      * @return
      *   the complete itinerary leg
      */
    infix def to(to: StationCode): ItineraryLeg =
      ItineraryLeg(train, from, to)

/** Represents a leg of an itinerary, made with a specific train. */
case class ItineraryLeg(train: Train, from: StationCode, to: StationCode):
  require(train.stations.contains(from), s"Train ${train.code} does not stop at $from")
  require(train.stations.contains(to), s"Train ${train.code} does not stop at $to")
  require(from != to, "Departure and arrival stations must be different")

  /** Return true if the leg utilize the reverse route of the train, false otherwise */
  def isForwardRoute: Boolean =
    val startIndex = train.route.stations.indexOf(from)
    val endIndex = train.route.stations.indexOf(to)
    startIndex < endIndex

  /** Returns the stations actually traveled on this leg. */
  def stationsOnLeg: List[StationCode] =
    val startIndex = train.route.stations.indexOf(from)
    val endIndex = train.route.stations.indexOf(to)
    if startIndex < endIndex then
      train.route.stations.slice(startIndex, endIndex + 1)
    else
      train.route.stations.slice(endIndex, startIndex + 1).reverse

  /** Total length of the leg (by summing the rails of the train's Route that are part of the leg). */
  def length: Double =
    val legStations = stationsOnLeg
    train.route.rails
      .filter(rail =>
        legStations.contains(rail.stationA) && legStations.contains(rail.stationB)
      )
      .map(_.length)
      .sum

/** Represents an itinerary composed of multiple train legs. */
case class Itinerary(legs: List[ItineraryLeg]):
  require(legs.nonEmpty, "Itinerary cannot be empty")

  /** Departure station of the itinerary. */
  def start: StationCode = legs.head.from

  /** Arrival station of the itinerary. */
  def end: StationCode = legs.last.to

  /** Visited stations in order. */
  def stations: List[StationCode] =
    legs.head.stationsOnLeg ++ legs.tail.flatMap(_.stationsOnLeg.drop(1))

  /** Total length of the itinerary. */
  def totalLength: Double = legs.map(_.length).sum

  /** Number of train changes to make. */
  def changeNumber: Int = legs.size - 1

  override def toString: String =
    val legsStr = legs.map { leg =>
      s"${leg.train.code.value}:${leg.from}→${leg.to}"
    }.mkString(", ")
    f"$start→$end | $legsStr | ${totalLength}%.1f km"
