package model.mapgrid

sealed trait PlacementError
object PlacementError:
  case class OutOfBounds(x: Int, y: Int) extends PlacementError
  case class InvalidPlacement(x: Int, y: Int, element: Cell) extends PlacementError
