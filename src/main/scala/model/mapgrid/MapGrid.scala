package model.mapgrid

object MapGrid:
  def empty(width: Int, height: Int): MapGrid =
    val emptyRow = Vector.fill(width)(EmptyCell)
    val grid = Vector.fill(height)(emptyRow)
    MapGrid(width, height, grid)

case class MapGrid(width: Int, height: Int, cells: Vector[Vector[Cell]], stationCounter: Int = 0):

  private val cardinalOffsets = Seq((0, -1), (-1, 0), (1, 0), (0, 1))
  private val diagonalOffsets = Seq((-1, -1), (1, -1), (-1, 1), (1, 1))

  def getStationsNumber: Int = stationCounter

  def place(x: Int, y: Int, cellType: CellType): Either[PlacementError, MapGrid] =
    cellType match
      case rail: RailType =>
        placeGenericRail(x, y, rail)
      case BigStationType =>
        placeBigStation(x, y)
      case SmallStationType =>
        placeSmallStation(x, y)
      case EmptyType => Right(copy())

  /** Check if a coordinate is inside grid bounds
    * @param x
    *   The x-coordinate to check.
    * @param y
    *   The y-coordinate to check.
    * @return
    *   True if the coordinate is inside grid bounds, False otherwise
    */
  private def isInBounds(x: Int, y: Int): Boolean = x >= 0 && y >= 0 && x < width && y < height

  /** Checks if the cell at the specified coordinates is empty.
    *
    * @param x
    *   The x-coordinate of the cell.
    * @param y
    *   The y-coordinate of the cell.
    * @return
    *   True if the cell is empty, False otherwise.
    */
  private def isEmpty(x: Int, y: Int): Boolean = cells(y)(x) == EmptyCell

  /** Checks whether there are any station pieces adjacent to the specified cell, considering all neighboring cells
    * (both cardinal and diagonal).
    *
    * @param x
    *   The x-coordinate of the cell to check around.
    * @param y
    *   The y-coordinate of the cell to check around.
    * @return
    *   True if at least one adjacent cell contains a StationPiece, False otherwise.
    */
  private def hasNearStations(x: Int, y: Int): Boolean =
    val stationCount = adjacentCells(x, y).count {
      case Some(_: StationPiece) => true
      case _ => false
    }
    stationCount > 0

  /** Returns all adjacent cells around the specified coordinate, including both cardinal (up, down, left, right) and
    * diagonal neighbors.
    *
    * @param x
    *   The x-coordinate of the reference cell.
    * @param y
    *   The y-coordinate of the reference cell.
    * @return
    *   A sequence of Option[Cell], each representing an adjacent cell if within bounds, or None if out of bounds.
    */
  private def adjacentCells(x: Int, y: Int): Seq[Option[Cell]] = cardinalCells(x, y) ++ diagonalCells(x, y)

  /** Returns the cells adjacent in the four cardinal directions (up, down, left, right) from the given coordinate.
    *
    * @param x
    *   The x-coordinate of the reference cell.
    * @param y
    *   The y-coordinate of the reference cell.
    * @return
    *   A sequence of Option[Cell], one for each cardinal neighbor cell, or None if the neighbor is out of grid bounds.
    */
  private def cardinalCells(x: Int, y: Int): Seq[Option[Cell]] = getCells(x, y, cardinalOffsets)

  /** Returns the cells adjacent in the four diagonal directions (top-left, top-right, bottom-left, bottom-right) from
    * the given coordinate.
    *
    * @param x
    *   The x-coordinate of the reference cell.
    * @param y
    *   The y-coordinate of the reference cell.
    * @return
    *   A sequence of Option[Cell], one for each diagonal neighbor cell, or None if the neighbor is out of grid bounds.
    */
  private def diagonalCells(x: Int, y: Int): Seq[Option[Cell]] = getCells(x, y, diagonalOffsets)

  /** Helper method to get cells at positions relative to (x, y) by given offsets.
    *
    * For each offset (dx, dy), it calculates the target coordinates (x + dx, y + dy) and returns Some(cell) if the
    * position is inside the grid bounds, or None otherwise.
    *
    * @param x
    *   The x-coordinate of the reference cell.
    * @param y
    *   The y-coordinate of the reference cell.
    * @param deltas
    *   A sequence of coordinate offsets relative to (x, y).
    * @return
    *   A sequence of Option[Cell], each representing a neighbor cell or None if out of bounds.
    */
  private def getCells(x: Int, y: Int, deltas: Seq[(Int, Int)]): Seq[Option[Cell]] =
    deltas.map { case (dx, dy) =>
      val nx = x + dx
      val ny = y + dy
      if isInBounds(nx, ny) then Some(cells(ny)(nx)) else None
    }

  /** Attempts to place a Small Station at the specified coordinates.
    *
    * The method performs the following checks before placement:
    *   - The coordinates must be within the grid bounds.
    *   - The target cell must be empty.
    *   - There must be no adjacent stations nearby to avoid station overcrowding.
    *
    * If any of these conditions fail, it returns a corresponding PlacementError. Otherwise, it assigns a new station ID
    * to the specified cell and returns a new MapGrid instance with the updated cell and incremented station counter.
    *
    * @param x
    *   The x-coordinate where the Small Station should be placed.
    * @param y
    *   The y-coordinate where the Small Station should be placed.
    * @return
    *   Either a PlacementError indicating why placement failed, or the updated MapGrid with the Small Station placed.
    */
  private def placeSmallStation(x: Int, y: Int): Either[PlacementError, MapGrid] =
    if !isInBounds(x, y) then return Left(PlacementError.OutOfBounds(x, y))
    if !isEmpty(x, y) then return Left(PlacementError.NonEmptyCell(x, y))
    if hasNearStations(x, y) then return Left(PlacementError.InvalidPlacement(x, y, SmallStationType))

    val nextId = stationCounter + 1
    Right(copy(
      stationCounter = nextId,
      cells = cells.updated(y, cells(y).updated(x, SmallStationPiece(nextId)))
    ))

  /** Attempts to place a Big Station centered at the specified coordinates.
    *
    * A Big Station occupies a 3x3 square of cells, with the given (centerX, centerY) as the center. This method
    * verifies that all 9 cells in the 3x3 area are:
    *   - Inside the bounds of the grid,
    *   - Empty (no other cell occupied),
    *   - Not adjacent to any other station (to avoid station overlap or proximity violations).
    *
    * If any of these conditions are not met, placement fails and returns a PlacementError. Otherwise, it assigns a new
    * station ID to all 9 cells of the Big Station and returns a new MapGrid instance with the updated cells and
    * incremented station counter.
    *
    * @param centerX
    *   The x-coordinate of the center cell where the Big Station will be placed.
    * @param centerY
    *   The y-coordinate of the center cell where the Big Station will be placed.
    * @return
    *   Either a PlacementError indicating why placement failed, or the updated MapGrid with the Big Station placed.
    */
  private def placeBigStation(centerX: Int, centerY: Int): Either[PlacementError, MapGrid] =
    val deltas = for dx <- -1 to 1; dy <- -1 to 1 yield (dx, dy)

    // Verify that all cells are valid for a station
    val allPlacementsValid = deltas.forall { (dx, dy) =>
      val x = centerX + dx
      val y = centerY + dy
      isInBounds(x, y) && isEmpty(x, y) && !hasNearStations(x, y)
    }

    if !allPlacementsValid then
      Left(PlacementError.InvalidPlacement(centerX, centerY, BigStationType))
    else
      val nextId = stationCounter + 1
      val newCells = deltas.foldLeft(cells) { case (grid, (dx, dy)) =>
        val x = centerX + dx
        val y = centerY + dy
        if (x == centerX && y == centerY) then
          grid.updated(y, grid(y).updated(x, BigStationCenterPiece(nextId)))
        else
          grid.updated(y, grid(y).updated(x, BigStationBorderPiece(nextId)))
      }
      Right(copy(
        stationCounter = nextId,
        cells = newCells
      ))

  /** Attempts to place a RailPiece at the specified coordinates. Validates the placement based on adjacent cells and
    * returns an updated MapGrid or an error. Rail placement rules:
    *   - A RailPiece should handle a big station as a single piece.
    *   - A RailPiece can be placed if there are 2 or fewer adjacent StationPieces or RailPieces.
    *
    * @param x
    *   The x-coordinate where the RailPiece should be placed.
    * @param y
    *   The y-coordinate where the RailPiece should be placed.
    * @param railType
    *   The Type of rail to be placed.
    * @return
    *   Either a PlacementError or the updated MapGrid.
    */
  private def placeGenericRail(x: Int, y: Int, railType: RailType): Either[PlacementError, MapGrid] =
    if !isInBounds(x, y) then return Left(PlacementError.OutOfBounds(x, y))
    if !isEmpty(x, y) then return Left(PlacementError.NonEmptyCell(x, y))
    railType match
      case MetalRailType => placeRail(x, y, railType, MetalRailPiece())
      case TitaniumRailType => placeRail(x, y, railType, TitaniumRailPiece())

  /** Check if placing a RailPiece in a certain position creates a 2x2 square of rails which would invalidate the
    * designed railway
    * @param x
    *   The x-coordinate where the RailPiece would be placed.
    * @param y
    *   The y-coordinate where the RailPiece would be placed.
    * @param railPiece
    *   The RailPiece to be placed.
    * @return
    *   True if the new rail piece would create a 2x2 square, false otherwise
    */
  private def createsSquare(x: Int, y: Int, railPiece: RailPiece): Boolean =
    val directions = Seq(
      (0, 0),
      (-1, 0),
      (0, -1),
      (-1, -1)
    ) // possible angles of the 2x2 square

    directions.exists { case (dx, dy) =>
      val x0 = x + dx
      val y0 = y + dy

      // Check if the 2x2 square is inside grid bounds
      if isInBounds(x0, y0) && isInBounds(x0 + 1, y0 + 1) then
        val cellsToCheck = Seq(
          (x0, y0),
          (x0 + 1, y0),
          (x0, y0 + 1),
          (x0 + 1, y0 + 1)
        )

        // Count how many square pieces are equal to railPiece (same type)
        val sameRailCount = cellsToCheck.count {
          case (cx, cy) =>
            if cx == x && cy == y then true // new cell to place
            else cells(cy)(cx) == railPiece
        }

        sameRailCount == 4 // complete square case
      else false
    }

  /** Attempts to place a specific Rail Piece into the grid. It checks if the adjacent cells allow a rail to be placed
    * or not.
    * @param x
    *   The x-coordinate where the RailPiece should be placed.
    * @param y
    *   The y-coordinate where the RailPiece should be placed.
    * @param railType
    *   The Type of rail to be placed.
    * @param railPiece
    *   The RailPiece to be placed.
    * @return
    *   Either a PlacementError or the updated MapGrid.
    */
  private def placeRail(x: Int, y: Int, railType: RailType, railPiece: RailPiece): Either[PlacementError, MapGrid] =
    val cardinal = cardinalCells(x, y)

    val smallStationCount = cardinal.count {
      case Some(SmallStationPiece(_)) => true
      case _ => false
    }

    val bigStationCount = cardinal.count {
      case Some(BigStationBorderPiece(_)) => true
      case _ => false
    }

    val sameRailCount = cardinal.count {
      case Some(piece) => piece == railPiece
      case _ => false
    }

    if smallStationCount + bigStationCount + sameRailCount == 0 || sameRailCount > 2 then
      Left(PlacementError.InvalidPlacement(x, y, railType))
    else if smallStationCount + bigStationCount + sameRailCount > 2 then
      Left(PlacementError.InvalidPlacement(x, y, railType))
    else if createsSquare(x, y, railPiece) then
      Left(PlacementError.InvalidPlacement(x, y, railType)) // Error if a 2x2 square would be formed
    else
      Right(copy(cells = cells.updated(y, cells(y).updated(x, railPiece))))
