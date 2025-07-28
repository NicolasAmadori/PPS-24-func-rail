package model.railway

trait StationsOperations[S <: StationsOperations[S]]:
  self: S =>
  def stations: List[Station]

  def withStations(newStations: List[Station]): S

  def addStation(station: Station): S =
    if stations.contains(station) then self
    else withStations(stations :+ station)

trait RailsOperations[S <: RailsOperations[S]]:
  self: S =>
  def rails: List[Rail]

  def withRails(newRails: List[Rail]): S

  def addRail(rail: Rail): S =
    if rails.contains(rail) then self
    else withRails(rails :+ rail)
