package model.mapgrid

import utils.ErrorMessage

sealed trait PlacementError extends ErrorMessage
object PlacementError:
  case class OutOfBounds(x: Int, y: Int) extends PlacementError
  case class InvalidPlacement(x: Int, y: Int, element: Cell) extends PlacementError
