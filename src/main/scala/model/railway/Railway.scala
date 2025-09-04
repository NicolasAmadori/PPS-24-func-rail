package model.railway

import model.entities.EntityCodes.StationCode
import model.entities.{Rail, Station}

/** Trait to model the railway as a list of nodes (stations) and a list of edges (rails) */
trait Railway:
  def stations: List[Station]
  def stationCodes: List[StationCode] = stations.map(_.code)
  def rails: List[Rail]

case class RailwayImpl(
    stations: List[Station],
    rails: List[Rail]
) extends Railway
    with StationsOperations[RailwayImpl]
    with RailsOperations[RailwayImpl]:

  override def withStations(newStations: List[Station]): RailwayImpl = copy(stations = newStations)

  override def withRails(newRails: List[Rail]): RailwayImpl = copy(rails = newRails)

object Railway:
  def empty: RailwayImpl = RailwayImpl(Nil, Nil)

  /** Creates a railway with stations */
  def withStations(stations: List[Station]): RailwayImpl = empty.withStations(stations)

  /** Creates a railway with rails */
  def withRails(rails: List[Rail]): RailwayImpl = empty.withRails(rails)
