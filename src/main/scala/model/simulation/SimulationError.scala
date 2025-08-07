package model.simulation

import model.railway.Domain.TrainCode
import utils.ErrorMessage

sealed trait SimulationError extends ErrorMessage

object SimulationError:
  case class EmptyTrainName() extends SimulationError:
    override def toString: String = "Train names cannot be empty"
  case class InvalidRoute(code: TrainCode) extends SimulationError:
    override def toString: String = s"Invalid route for train $code"
  case class InvalidDeparture(code: TrainCode) extends SimulationError:
    override def toString: String = s"Invalid departure station for train $code"
