package model.simulation

import model.railway.Rail
import model.railway.Domain.StationCode

/** Represents a specific path between stations.
  * @param rails
  *   The sequence of rails that make up the route.
  */
case class Route(rails: List[Rail]):
  /** Calculates the total length of the route by summing the length of each rail. */
  def length: Double = rails.map(_.length).sum

  /** Returns the station where the route begins. */
  def startStation: Option[StationCode] = rails.headOption.map(_.stationA)

  /** Returns the station where the route ends. */
  def endStation: Option[StationCode] = rails.lastOption.map(_.stationB)

  /** Provides a string representation of the route, showing the sequence of stations and rail lengths. */
  override def toString: String =
    if rails.isEmpty then
      "Empty Route"
    else
      val start = s"${rails.head.stationA}"
      val path = rails.map(rail => s" --(${rail.length})--> ${rail.stationB}").mkString("")
      s"Route: $start$path"
