package model.util

import model.mapgrid.{BigStationBorderPiece, BigStationCenterPiece, BigStationType, Cell, MapGrid, MetalRailPiece, MetalRailType, SmallStationPiece, SmallStationType, TitaniumRailPiece, TitaniumRailType}
import model.railway.Rail.{metalRail, titaniumRail}
import model.railway.Station.{bigStation, smallStation}
import model.railway.{Rail, Railway, Station}

object RailwayMapper:

  val SMALL_STATION_PREFIX = "ST"
  val BIG_STATION_PREFIX = "BST"

  var stationCounter = 0
  var railCounter = 0

  var alreadyCheckedCells: List[(Int, Int)] = List.empty

  def convert(mapGrid: MapGrid): Railway =
    val smallStations: List[(Station, Int, Int)] = extractSmallStations(mapGrid)
    val bigStations: List[(Station, Int, Int)] = extractBigStations(mapGrid)
    val rails: List[Rail] = extractRails(mapGrid)(smallStations)
    Railway
      .withStations(smallStations.map(_._1) ++ bigStations.map(_._1))
      .withRails(rails)

  private def extractSmallStations(mapGrid: MapGrid): List[(Station, Int, Int)] =
    val allCellsWithCoordinates =
      for
        (row, y) <- mapGrid.cells.zipWithIndex
        (cell, x) <- row.zipWithIndex
      yield ((x, y), cell)
    allCellsWithCoordinates.collect {
      case ((x, y), SmallStationPiece(id)) => (smallStation(s"$SMALL_STATION_PREFIX$id"), x, y)
    }.toList

  private def extractBigStations(mapGrid: MapGrid): List[(Station, Int, Int)] =
    val allCellsWithCoordinates =
      for
        (row, y) <- mapGrid.cells.zipWithIndex
        (cell, x) <- row.zipWithIndex
      yield ((x, y), cell)

    allCellsWithCoordinates
      .collect {
        case ((x, y), BigStationCenterPiece(id)) =>
          (bigStation(s"$BIG_STATION_PREFIX$id"), x, y)
      }
      .toList

  private def getCardinalCells(mapGrid: MapGrid)(x: Int, y: Int): Seq[Option[(Cell, Int, Int)]] =
    val cardinalOffsets = Seq((0, -1), (-1, 0), (1, 0), (0, 1))
    cardinalOffsets.map { case (dx, dy) =>
      val nx = x + dx
      val ny = y + dy
      if mapGrid.isInBounds(nx, ny) then Some((mapGrid.cells(ny)(nx), nx, ny)) else None
    }

  private def extractRails(mapGrid: MapGrid)(smallStations: List[(Station, Int, Int)]): List[Rail] =
    var railCounter = 0

    smallStations.flatMap { (s, x, y) =>
      getCardinalCells(mapGrid)(x, y).flatMap {
        case Some((cell, nx, ny)) =>
          cell match
            case _: MetalRailPiece =>
              railCounter += 1
              println("" + (x, y) + " ->")
              followMetalRails(mapGrid)(nx, ny, x, y, metalRail(railCounter, 0, s.code.toString, ""))
            case _: TitaniumRailPiece =>
              railCounter += 1
              followTitaniumRails(mapGrid)(nx, ny, x, y, titaniumRail(railCounter, 0, s.code.toString, ""))
            case _ => None
        case None => None
      }
    }

  private def followMetalRails(mapGrid: MapGrid)(
      x: Int,
      y: Int,
      previousX: Int,
      previousY: Int,
      rail: Rail
  ): Option[Rail] =
    val cardinalCells = getCardinalCells(mapGrid)(x, y).collect {
      case Some((cell, nx, ny)) if (nx != previousX || ny != previousY) && !alreadyCheckedCells.contains((nx, ny)) =>
        (cell, nx, ny)
    }
    alreadyCheckedCells = (x, y) :: alreadyCheckedCells
    val nearMetalRails = cardinalCells.filter {
      case (cell, _, _) => cell.cellType == MetalRailType
    }.toList

    val nearStations = cardinalCells.filter {
      case (cell, _, _) => cell.cellType == SmallStationType || cell.cellType == BigStationType
    }.toList

    if nearMetalRails.size > 1 then
      println("Too much metal rails found.")
      return None

    if nearStations.size > 1 then
      println("Too much stations found.")
      return None

    if nearMetalRails.isEmpty && nearStations.isEmpty then
      println("No more metal rails found. Dead rail")
      return None

    if nearMetalRails.isEmpty && nearStations.nonEmpty then
      val nearStation = nearStations.head._1

      return Some(metalRail(
        rail.code.toString.toInt,
        rail.length + 1,
        rail.stationA.toString,
        nearStation match
          case SmallStationPiece(id) => s"$SMALL_STATION_PREFIX$id"
          case BigStationBorderPiece(id) => s"$BIG_STATION_PREFIX$id"
          case _ => ""
      ))

    val newRail = metalRail(rail.code.toString.toInt, rail.length + 1, rail.stationA.toString, rail.stationB.toString)
    println("" + (previousX, previousY) + " -> " + (x, y) + ". new rail: " + newRail)
    val nextMetalRail = nearMetalRails.head

    followMetalRails(mapGrid)(nextMetalRail._2, nextMetalRail._3, x, y, newRail)

  private def followTitaniumRails(mapGrid: MapGrid)(
      x: Int,
      y: Int,
      previousX: Int,
      previousY: Int,
      rail: Rail
  ): Option[Rail] =
    val cardinalCells = getCardinalCells(mapGrid)(x, y).collect {
      case Some((cell, nx, ny)) if (nx != previousX || ny != previousY) && !alreadyCheckedCells.contains((nx, ny)) =>
        (cell, nx, ny)
    }
    alreadyCheckedCells = (x, y) :: alreadyCheckedCells
    val nearTitaniumRails = cardinalCells.filter {
      case (cell, _, _) => cell.cellType == TitaniumRailType
    }.toList

    val nearStations = cardinalCells.filter {
      case (cell, _, _) => cell.cellType == SmallStationType || cell.cellType == BigStationType
    }.toList

    if nearTitaniumRails.size > 1 then
      println("Too much titanium rails found.")
      return None

    if nearStations.size > 1 then
      println("Too much stations found.")
      return None

    if nearTitaniumRails.isEmpty && nearStations.isEmpty then
      println("No more titanium rails found. Dead rail")
      return None

    if nearTitaniumRails.isEmpty && nearStations.nonEmpty then
      val nearStation = nearStations.head._1

      return Some(titaniumRail(
        rail.code.toString.toInt,
        rail.length + 1,
        rail.stationA.toString,
        nearStation match
          case SmallStationPiece(id) => s"$SMALL_STATION_PREFIX$id"
          case BigStationBorderPiece(id) => s"$BIG_STATION_PREFIX$id"
          case _ => ""
      ))

    val newRail =
      titaniumRail(rail.code.toString.toInt, rail.length + 1, rail.stationA.toString, rail.stationB.toString)
    println("" + (previousX, previousY) + " -> " + (x, y) + ". new rail: " + newRail)
    val nextTitaniumRail = nearTitaniumRails.head

    followTitaniumRails(mapGrid)(nextTitaniumRail._2, nextTitaniumRail._3, x, y, newRail)
