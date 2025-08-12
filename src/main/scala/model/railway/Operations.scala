package model.railway

trait StationsOperations[S <: StationsOperations[S]]:
  self: S =>
  def stations: List[Station]

  def withStations(newStations: List[Station]): S

trait RailsOperations[S <: RailsOperations[S]]:
  self: S =>
  def rails: List[Rail]

  def withRails(newRails: List[Rail]): S
