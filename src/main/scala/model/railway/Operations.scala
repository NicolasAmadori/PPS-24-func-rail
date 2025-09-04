package model.railway

import model.entities.{Rail, Station}

/** Trait to model operations on a list of stations */
trait StationsOperations[S <: StationsOperations[S]]:
  self: S =>
  def stations: List[Station]

  def withStations(newStations: List[Station]): S

/** Trait to model operations on a list of rails */
trait RailsOperations[S <: RailsOperations[S]]:
  self: S =>
  def rails: List[Rail]

  def withRails(newRails: List[Rail]): S
