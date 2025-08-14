package model.mapgrid

import utils.ErrorMessage

sealed trait PlacementError extends ErrorMessage
object PlacementError:
  case class OutOfBounds(x: Int, y: Int) extends PlacementError:
    override def toString: String = "The selected coordinates are out of the grid bounds"
  case class NonEmptyCell(x: Int, y: Int) extends PlacementError:
    override def toString: String = "The selected coordinates are already occupied on the map grid"
  case class InvalidPlacement(x: Int, y: Int, cellType: CellType) extends PlacementError:
    override def toString: String = s"The selected coordinates are invalid for a ${cellType.toString} placement"
  case class NonIsolatedStation(x: Int, y: Int) extends PlacementError:
    override def toString: String = s"The selected station can't be erased since it is connected to at least 1 rail"
