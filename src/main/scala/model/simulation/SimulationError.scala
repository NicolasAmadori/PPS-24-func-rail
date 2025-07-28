package model.simulation

sealed trait SimulationError

object SimulationError:
  case class InvalidRoute() extends SimulationError
