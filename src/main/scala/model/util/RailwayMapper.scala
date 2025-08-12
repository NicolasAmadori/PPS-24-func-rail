/** @file
  *   This object is responsible for mapping a `MapGrid` representation of a game world into a `Railway` object, which
  *   models a network of stations and rails. It navigates the grid to identify different types of stations and rails
  *   and builds the railway infrastructure.
  */
package model.util

import model.mapgrid.{BigStationBorderPiece, BigStationCenterPiece, BigStationType, Cell, CellType, MapGrid, MetalRailPiece, MetalRailType, SmallStationPiece, SmallStationType, TitaniumRailPiece, TitaniumRailType}
import model.entities.EntityCodes.{RailCode, StationCode}
import model.entities.{MetalRail, Rail, Station, TitaniumRail}
import model.entities.Rail.{metalRail, titaniumRail}
import model.entities.Station.{bigStation, smallStation}
import model.railway.Railway

import scala.annotation.tailrec

object RailwayMapper:

  private val SMALL_STATION_PREFIX: String = "ST"
  private val BIG_STATION_PREFIX: String = "BST"

  val BLOCK_TO_KM_MULTIPLIER: Int = 50

  /** A set of coordinates for cells that have already been visited during rail mapping. This prevents infinite loops
    * and redundant processing.
    */

  /** Converts a given `MapGrid` into a `Railway` object.
    *
    * This is the main entry point of the mapping process. It orchestrates the extraction of stations and rails from the
    * grid and assembles them into a coherent `Railway` object.
    *
    * @param mapGrid
    *   The `MapGrid` to be converted.
    * @return
    *   A `Railway` object representing the discovered network of stations and rails.
    */
  def convert(mapGrid: MapGrid): Railway =
    val smallStations: List[(Station, Int, Int)] = extractSmallStations(mapGrid)
    val bigStations: List[(Station, Int, Int)] = extractBigStations(mapGrid)
    val expandedBigStations: List[(Station, Int, Int)] = expandBigStations(mapGrid)(bigStations)

    val allStations: List[(Station, Int, Int)] = smallStations ++ expandedBigStations
    val rails: List[Rail] = extractRails(mapGrid)(allStations)
    val invertedRails: List[Rail] = rails.map {
      case r: MetalRail =>
        metalRail(RailCode.value(r.code), r.length, StationCode.value(r.stationB), StationCode.value(r.stationA))
      case r: TitaniumRail =>
        titaniumRail(RailCode.value(r.code), r.length, StationCode.value(r.stationB), StationCode.value(r.stationA))
    }
    Railway
      .withStations(smallStations.map(_._1) ++ bigStations.map(_._1))
      .withRails(rails ++ invertedRails)

  /** Expands the representation of big stations to include their surrounding border cells.
    *
    * A big station is represented by a central piece and multiple border pieces. This method associates the big station
    * object with all its border cell coordinates.
    *
    * @param mapGrid
    *   The `MapGrid` containing the big stations.
    * @param bigStations
    *   A list of tuples, each containing a big station object and its center coordinates.
    * @return
    *   A bigger list of tuples, where each original big station is associated with all its border cells. The size of
    *   the output list will be exactly 8 times bigger than the original
    */
  private def expandBigStations(mapGrid: MapGrid)(bigStations: List[(Station, Int, Int)]): List[(Station, Int, Int)] =
    bigStations.flatMap { (station, centerX, centerY) =>
      getSurroundingCells(mapGrid)(centerX, centerY).collect {
        case Some(_, borderX, borderY) => (station, borderX, borderY)
      }
    }

  /** Extracts all small stations from the `MapGrid`.
    *
    * It scans the grid for `SmallStationPiece` cells and creates a `Station` object for each, associating it with its
    * coordinates.
    *
    * @param mapGrid
    *   The `MapGrid` to search.
    * @return
    *   A list of tuples, each containing a small station object and its coordinates.
    */
  private def extractSmallStations(mapGrid: MapGrid): List[(Station, Int, Int)] =
    cellsWithCoords(mapGrid).collect {
      case ((x, y), SmallStationPiece(id)) =>
        (smallStation(s"$SMALL_STATION_PREFIX$id"), x, y)
    }.toList

  /** Extracts all big stations from the `MapGrid`.
    *
    * It looks for `BigStationCenterPiece` cells, which are the central points of big stations, and creates a `Station`
    * object for each.
    *
    * @param mapGrid
    *   The `MapGrid` to search.
    * @return
    *   A list of tuples, each containing a big station object and its center coordinates.
    */
  private def extractBigStations(mapGrid: MapGrid): List[(Station, Int, Int)] =
    cellsWithCoords(mapGrid).collect {
      case ((x, y), BigStationCenterPiece(id)) =>
        (bigStation(s"$BIG_STATION_PREFIX$id"), x, y)
    }.toList

  /** Flattens the 2D grid of cells into a sequence of tuples, each containing a cell's coordinates and the cell itself.
    *
    * @param mapGrid
    *   The `MapGrid` to process.
    * @return
    *   A sequence of `((Int, Int), Cell)` tuples.
    */
  private def cellsWithCoords(mapGrid: MapGrid): Seq[((Int, Int), Cell)] =
    for
      (row, y) <- mapGrid.cells.zipWithIndex
      (cell, x) <- row.zipWithIndex
    yield ((x, y), cell)

  /** Gets the cells that are cardinally (up, down, left, right) adjacent to a given cell.
    *
    * @param mapGrid
    *   The `MapGrid` to operate on.
    * @param x
    *   The x-coordinate of the central cell.
    * @param y
    *   The y-coordinate of the central cell.
    * @return
    *   A sequence of `Option[(Cell, Int, Int)]` representing the cardinal cells and their coordinates. `None` is used
    *   for coordinates that are out of bounds.
    */
  private def getCardinalCells(mapGrid: MapGrid)(x: Int, y: Int): Seq[Option[(Cell, Int, Int)]] =
    val cardinalOffsets = Seq((0, -1), (-1, 0), (1, 0), (0, 1))
    cardinalOffsets.map { case (dx, dy) =>
      val nx = x + dx
      val ny = y + dy
      if mapGrid.isInBounds(nx, ny) then Some((mapGrid.cells(ny)(nx), nx, ny)) else None
    }

  /** Gets all 8 surrounding cells (including diagonals) of a given cell, excluding the center cell itself.
    *
    * This is primarily used for finding the border pieces of a big station.
    *
    * @param mapGrid
    *   The `MapGrid` to operate on.
    * @param x
    *   The x-coordinate of the central cell.
    * @param y
    *   The y-coordinate of the central cell.
    * @return
    *   A sequence of `Option[(Cell, Int, Int)]` representing the surrounding cells and their coordinates. `None` is
    *   used for coordinates that are out of bounds.
    */
  private def getSurroundingCells(mapGrid: MapGrid)(x: Int, y: Int): Seq[Option[(Cell, Int, Int)]] =
    val surroundingOffsets =
      for
        dx <- -1 to 1
        dy <- -1 to 1
        if !(dx == 0 && dy == 0) // exclude the bigstation center
      yield (dx, dy)

    surroundingOffsets.map { case (dx, dy) =>
      val nx = x + dx
      val ny = y + dy
      if mapGrid.isInBounds(nx, ny) then Some((mapGrid.cells(ny)(nx), nx, ny)) else None
    }

  /** Extracts all railway tracks (rails) from the `MapGrid`.
    *
    * It starts from each station and recursively follows connected rail pieces to build complete rail segments between
    * stations.
    *
    * @param mapGrid
    *   The `MapGrid` to search for rails.
    * @param stations
    *   A list of station objects and their coordinates, used as starting points.
    * @return
    *   A list of `Rail` objects representing all the discovered railway tracks.
    */
  private def extractRails(mapGrid: MapGrid)(stations: List[(Station, Int, Int)]): List[Rail] =
    var railCounter: Int = 0
    var alreadyCheckedCells: Set[(Int, Int)] = Set.empty

    stations.flatMap { (station, stationX, stationY) =>
      getCardinalCells(mapGrid)(stationX, stationY).collect {
        case Some((cell: MetalRailPiece, nx, ny)) =>
          (nx, ny, MetalRailType, metalRail)
        case Some((cell: TitaniumRailPiece, nx, ny)) =>
          (nx, ny, TitaniumRailType, titaniumRail)
      }.flatMap { (nx, ny, railType, createRailFn) =>
        val railId =
          railCounter += 1
          railCounter
        followRails(mapGrid)(
          nx,
          ny,
          stationX,
          stationY,
          createRailFn(railId, 0, StationCode.value(station.code), ""),
          railType,
          createRailFn,
          alreadyCheckedCells
        ) match
          case Some((updatedSet, rail)) =>
            alreadyCheckedCells = updatedSet
            Some(rail)
          case None =>
            railCounter -= 1
            None
      }
    }

  /** Recursively follows a sequence of rail pieces from a starting point until a station or an endpoint is found.
    *
    * This is a tail-recursive function that traverses the grid to determine the length and endpoints of a single rail
    * segment. It uses `alreadyCheckedCells` to avoid loops.
    *
    * @param mapGrid
    *   The `MapGrid` being traversed.
    * @param x
    *   The x-coordinate of the current rail cell.
    * @param y
    *   The y-coordinate of the current rail cell.
    * @param previousX
    *   The x-coordinate of the previous cell in the path.
    * @param previousY
    *   The y-coordinate of the previous cell in the path.
    * @param rail
    *   The `Rail` object being built.
    * @param railType
    *   The type of rail being followed (e.g., MetalRailType, TitaniumRailType).
    * @param createRailFunction
    *   A function to create a new `Rail` object with updated properties.
    * @param alreadyChecked
    *   A set of already controlled coordinates to avoid loop.
    * @return
    *   An `Option[(Set[(Int, Int)], Rail)]` containing the set of checked cells and the completed rail object if a
    *   valid path is found, otherwise `None`.
    */
  @tailrec
  private def followRails(mapGrid: MapGrid)(
      x: Int,
      y: Int,
      previousX: Int,
      previousY: Int,
      rail: Rail,
      railType: CellType,
      createRailFunction: (Int, Int, String, String) => Rail,
      alreadyChecked: Set[(Int, Int)]
  ): Option[(Set[(Int, Int)], Rail)] =
    if alreadyChecked.contains((x, y)) then
      return None

    val updatedChecked = alreadyChecked + ((x, y))
    val cardinalCells = getCardinalCells(mapGrid)(x, y).collect {
      case Some((cell, nx, ny)) if (nx != previousX || ny != previousY) && !alreadyChecked.contains((nx, ny)) =>
        (cell, nx, ny)
    }

    val nearRails = cardinalCells.filter {
      case (cell, _, _) => cell.cellType == railType
    }.toList

    val nearStations = cardinalCells.filter {
      case (cell, _, _) => cell.cellType == SmallStationType || cell.cellType == BigStationType
    }.toList

    if nearRails.size > 1 then
      return None // Too much valid rails found.

    if nearStations.size > 1 then
      return None // Too much stations found.

    if nearRails.isEmpty && nearStations.isEmpty then
      // No more valid rails found. Dead rail
      return None

    if nearRails.isEmpty && nearStations.nonEmpty then
      // Rail completed
      val nearStationCode: String = nearStations.head._1 match
        case SmallStationPiece(id) => s"$SMALL_STATION_PREFIX$id"
        case BigStationBorderPiece(id) => s"$BIG_STATION_PREFIX$id"
        case _ => ""

      if StationCode.value(rail.stationA) == nearStationCode then
        return None // The rail start and finish to the same station.

      return Some(
        updatedChecked,
        createRailFunction(
          RailCode.value(rail.code),
          rail.length + (1 * BLOCK_TO_KM_MULTIPLIER),
          StationCode.value(rail.stationA),
          nearStationCode
        )
      )

    val newRail =
      createRailFunction(
        RailCode.value(rail.code),
        rail.length + (1 * BLOCK_TO_KM_MULTIPLIER),
        StationCode.value(rail.stationA),
        StationCode.value(rail.stationB)
      )
    val nextRail = nearRails.head

    followRails(mapGrid)(nextRail._2, nextRail._3, x, y, newRail, railType, createRailFunction, updatedChecked)
