package model

trait Railway:
  def stations: List[Station]
  def trains: List[Train]
  def rails: List[Rail]

case class RailwayImpl(
    stations: List[Station],
    trains: List[Train],
    rails: List[Rail]
) extends Railway
    with TrainOperations[RailwayImpl]
    with StationsOperations[RailwayImpl]
    with RailsOperations[RailwayImpl]:
  override def withTrains(newTrains: List[Train]): RailwayImpl = copy(trains = newTrains)

  override def withStations(newStations: List[Station]): RailwayImpl = copy(stations = newStations)

  override def withRails(newRails: List[Rail]): RailwayImpl = copy(rails = newRails)

object Railway:
  def empty: RailwayImpl = RailwayImpl(Nil, Nil, Nil)

  def withStations(stations: List[Station]): RailwayImpl = empty.withStations(stations)

  def withTrains(trains: List[Train]): RailwayImpl = empty.withTrains(trains)

  def withRails(rails: List[Rail]): RailwayImpl = empty.withRails(rails)
