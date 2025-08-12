package model.simulation

import model.railway.EntityCodes.TrainCode
import utils.ErrorMessage

sealed trait SimulationError extends ErrorMessage

object SimulationError:
  case class NotStarted() extends SimulationError:
    override def toString: String = "The simulation has not been started yet"
  case class EmptyTrainName() extends SimulationError:
    override def toString: String = "Train names cannot be empty"
  case class InvalidRoute(name: String) extends SimulationError:
    override def toString: String = s"Invalid route for train $name"
  case class InvalidDeparture(name: String) extends SimulationError:
    override def toString: String = s"Invalid departure station for train $name"
  case class CannotComputeRoute(code: TrainCode) extends SimulationError:
    override def toString: String = s"Cannot compute $code route"
