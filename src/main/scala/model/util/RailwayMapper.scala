package model.util

import model.mapgrid.{BigStationPiece, MapGrid, SmallStationPiece}
import model.railway.Station.{bigStation, smallStation}
import model.railway.{Railway, Station}

object RailwayMapper:

  val SMALL_STATION_PREFIX = "ST"
  val BIG_STATION_PREFIX = "BST"

  var stationCounter = 0
  var railCounter = 0

  def convert(mapGrid: MapGrid): Railway =
    val smallStations: List[(Station, Int, Int)] = extractSmallStations(mapGrid)
    val bigStations: List[(Station, Int, Int)] = extractBigStations(mapGrid)
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
        case ((x, y), BigStationPiece(id)) =>
          (bigStation(s"$BIG_STATION_PREFIX$id"), x, y)
      }
      .distinctBy(_._1)
      .toList

//  private def extractRails(mapGrid: MapGrid)(smallStationsCoordinate: List[(Int, Int)]): List[Rail] = ???
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
