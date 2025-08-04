package model.util

import model.mapgrid.{BigStationPiece, MapGrid, RailPiece, SmallStationPiece}
import model.railway.Station.{bigStation, smallStation}
import model.railway.{Rail, Railway, Station}

object RailwayMapper:

  val SMALL_STATION_PREFIX = "ST"
  val BIG_STATION_PREFIX = "BST"

  var stationCounter = 0
  var railCounter = 0

  def convert(mapGrid: MapGrid): Railway =
    val smallStations: List[(Station, Int, Int)] = extractSmallStations(mapGrid)
    val bigStations: List[(Station, Int, Int)] = extractBigStations(mapGrid)
    println(smallStations.size)
    println(smallStations)
    println(bigStations.size)
    println(bigStations)
//    val rails: List[Rail] = extractRails(mapGrid)(smallStations.map(s => (s._2, s._3)))
    Railway
      .withStations(smallStations.map(_._1) ++ bigStations.map(_._1))
//      .withRails(rails)

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
        case ((x, y), BigStationPiece(id)) if isBigStationCenter(mapGrid, x, y) =>
          (bigStation(s"$BIG_STATION_PREFIX$id"), x, y)
      }.toList
//
//    val validCenters =
//      for
//        x <- 1 until mapGrid.width - 1
//        y <- 1 until mapGrid.height - 1
//        if isBigStationCenter(mapGrid, x, y)
//      yield (x, y)
//
//    validCenters.zipWithIndex.map { case ((x, y), idx) =>
//      (bigStation(s"$BIG_STATION_PREFIX$idx"), x, y)
//    }.toList

  private def isBigStationCenter(mapGrid: MapGrid, centerX: Int, centerY: Int): Boolean =
    val deltas = for dx <- -1 to 1; dy <- -1 to 1 yield (dx, dy)
    deltas.forall { case (dx, dy) =>
      val x = centerX + dx
      val y = centerY + dy
      mapGrid.cells(y)(x) match
        case BigStationPiece(_) => true
        case _ => false
    }

  private def extractRails(mapGrid: MapGrid)(smallStationsCoordinate: List[(Int, Int)]): List[Rail] = ???
//    smallStationsCoordinate.foreach((x, y) =>
//      mapGrid.adjacentCells(x, y)
//        .collect {
//          case Some(rp: RailPiece) => rp
//        }
//        .foreach(_ =>
//          followRails(mapGrid)(x, y)
//        )
//    )
//
//    List.empty

//  private def followRails(mapGrid: MapGrid)(startingX: Int, startingY: Int): Option[Rail] = Option.empty
