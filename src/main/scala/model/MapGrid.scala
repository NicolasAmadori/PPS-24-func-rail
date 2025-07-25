package model

object MapGrid:
  def empty(width: Int, height: Int): MapGrid =
    val emptyRow = Vector.fill(width)(EmptyCell)
    val grid = Vector.fill(height)(emptyRow)
    MapGrid(width, height, grid)

case class MapGrid(width: Int, height: Int, cells: Vector[Vector[Cell]]):

  def place(x: Int, y: Int, element: Cell): Either[PlacementError, MapGrid] =
    if !isInBounds(x, y) then
      Left(PlacementError.OutOfBounds(x, y))
    else if !canPlaceAt(x, y, element) then
      Left(PlacementError.InvalidPlacement(x, y, element))
    else
      Right(copy(cells = cells.updated(y, cells(y).updated(x, element))))

  def getStationsNumber: Int =
    cells.flatten.count {
      case _: StationPiece => true
      case _ => false
    }

  private def isInBounds(x: Int, y: Int): Boolean =
    x >= 0 && y >= 0 && x < width && y < height

  private def canPlaceAt(x: Int, y: Int, element: Cell): Boolean =
    element match
      case _: StationPiece =>
        val stationCount = adjacentCells(x, y).count {
          case Some(_: StationPiece) => true
          case _ => false
        }
        stationCount == 0

      case _: RailPiece =>
        val pieceCount = adjacentCells(x, y).count {
          case Some(_: StationPiece) => true
          case Some(_: RailPiece) => true
          case _ => false
        }
        pieceCount < 2

      case _ => false

  private def adjacentCells(x: Int, y: Int): Seq[Option[Cell]] =
    cardinalCells(x, y) ++ diagonalCells(x, y)

  private def cardinalCells(x: Int, y: Int): Seq[Option[Cell]] =
    val deltas = Seq(
      (0, -1),
      (-1, 0),
      (1, 0),
      (0, 1)
    )
    getCells(x, y, deltas)

  private def diagonalCells(x: Int, y: Int): Seq[Option[Cell]] =
    val deltas = Seq(
      (-1, -1),
      (1, -1),
      (-1, 1),
      (1, 1)
    )
    getCells(x, y, deltas)

  private def getCells(x: Int, y: Int, deltas: Seq[(Int, Int)]): Seq[Option[Cell]] =
    deltas.map { case (dx, dy) =>
      val nx = x + dx
      val ny = y + dy
      if isInBounds(nx, ny) then Some(cells(ny)(nx)) else None
    }
