package model.entities

import model.entities.EntityCodes.StationCode

/** Represents a specific path between stations.
  *
  * @param rails
  *   The sequence of rails that make up the route.
  */
case class Route(rails: List[Rail]):
  /** Calculates the total length of the route by summing the length of each rail. */
  def length: Double = rails.map(_.length).sum

  def railsCount: Int = rails.length

  /** @return true if there is no rail, false otherwise */
  def isEmpty: Boolean = rails.isEmpty

  /** @return a list of the stations visited by the route */
  def stations: List[StationCode] = rails.flatMap(rail => List(rail.stationA, rail.stationB)).distinct

  /** Returns the station where the route begins. */
  def startStation: Option[StationCode] = rails.headOption.map(_.stationA)

  /** Returns the station where the route ends. */
  private def endStation: Option[StationCode] = rails.lastOption.map(_.stationB)

  def isEndOfRoute(stationCode: StationCode): Boolean =
    stationCode == startStation.get || stationCode == endStation.get

  def getRailAt(index: Int): Rail = rails(index)

  def getEndStationAt(index: Int, forward: Boolean): StationCode =
    val rail = rails(index)
    if forward then rail.stationB else rail.stationA

  /** Provides a string representation of the route, showing the sequence of stations and rail lengths. */
  override def toString: String =
    if rails.isEmpty then
      "Empty Route"
    else
      val start = s"${rails.head.stationA}"
      val path = rails.map(rail => s" <--(${rail.length})--> ${rail.stationB}").mkString("")
      s"Route: $start$path"

object Route:
  /** Creates an empty Route */
  def empty: Route = Route(Nil)
