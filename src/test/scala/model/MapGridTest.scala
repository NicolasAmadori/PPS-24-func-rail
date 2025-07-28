package model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class MapGridTest extends AnyFlatSpec:

  "A MapGrid" should "have 0 stations when created" in {
    val mapGrid = MapGrid.empty(10, 10)
    mapGrid.getStationsNumber should be(0)
  }

  it should "allow a station to be successfully placed" in {
    val mapGrid = MapGrid.empty(10, 10)
    val result = mapGrid.place(2, 2, SmallStationPiece)

    result match
      case Right(updatedMap) =>
        updatedMap.getStationsNumber should be(1)
      case Left(err) =>
        fail(s"Expected successful placement, got error: $err")
  }

  it should "not allow placement outside the grid" in {
    val mapGrid = MapGrid.empty(10, 10)
    val invalidCoords = Seq(
      (10, 2),
      (2, 10),
      (-1, 2),
      (4, -2)
    )

    invalidCoords.foreach { (x, y) =>
      withClue(s"Failed with ($x, $y):") {
        mapGrid.place(x, y, SmallStationPiece) should matchPattern {
          case Left(PlacementError.OutOfBounds(_, _)) =>
        }
      }
    }
  }

  it should "not allow placement near another station" in {
    var mapGrid = MapGrid.empty(10, 10)

    mapGrid.place(3, 3, SmallStationPiece) match
      case Right(newMapGrid) => mapGrid = newMapGrid
      case Left(err) => fail(s"Initial station placement failed: $err")

    val invalidCoords = Seq(
      (2, 2),
      (2, 3),
      (2, 4),
      (3, 2),
      (3, 4),
      (4, 2),
      (4, 3),
      (4, 4)
    )

    invalidCoords.foreach { (x, y) =>
      withClue(s"Failed with ($x, $y):") {
        mapGrid.place(x, y, SmallStationPiece) should matchPattern {
          case Left(PlacementError.InvalidPlacement(_, _, _)) =>
        }
      }
    }
  }

  "A MapGrid" should "allow placing a RailPiece on an empty cell with no adjacent stations or rails" in {
    val grid = MapGrid.empty(3, 3)
    val result = grid.place(1, 1, MetalRailPiece)
    assert(result.isRight)
  }

  it should "not allow placing a RailPiece if there are 2 or more adjacent StationPieces or RailPieces" in {
    val grid = MapGrid.empty(3, 3)
      .place(0, 1, MetalRailPiece).toOption.get
      .place(2, 1, TitaniumRailPiece).toOption.get

    val result = grid.place(1, 1, MetalRailPiece)
    assert(result.isLeft)
  }

  it should "allow placing a RailPiece adjacent to a StationPiece" in {
    val grid = MapGrid.empty(3, 3)
      .place(0, 1, SmallStationPiece).toOption.get

    val result = grid.place(1, 1, MetalRailPiece)
    assert(result.isRight)
  }

  it should "allow placing a RailPiece in an empty grid cell if the rules permit" in {
    val grid = MapGrid.empty(3, 3)

    val result = grid.place(1, 1, TitaniumRailPiece)
    assert(result.isRight)
  }


  it should "allow placing a BigStationPiece when there's enough space" in {
    val grid = MapGrid.empty(10, 10)
    val centerX = 5
    val centerY = 5
    val result = grid.place(centerX, centerY, BigStationPiece)

    result match
      case Right(updated) =>
        updated.getStationsNumber should be(1)
        val surrounding = for dx <- -1 to 1; dy <- -1 to 1 yield (centerX + dx, centerY + dy)
        surrounding.foreach { (x, y) =>
          updated.cells(y)(x) shouldBe BigStationPiece
        }

      case Left(err) =>
        fail(s"Expected successful BigStationPiece placement, got: $err")
  }

  it should "not allow placing a BigStationPiece if part of it is out of bounds" in {
    val grid = MapGrid.empty(3, 3)
    val result = grid.place(0, 0, BigStationPiece) // angolo in alto a sinistra
    result should matchPattern {
      case Left(PlacementError.InvalidPlacement(_, _, _)) =>
    }
  }

  it should "not allow placing a BigStationPiece too close to a SmallStationPiece" in {
    val grid = MapGrid.empty(10, 10)
      .place(1, 1, SmallStationPiece).toOption.get

    val result = grid.place(3, 3, BigStationPiece) // troppo vicino
    result should matchPattern {
      case Left(PlacementError.InvalidPlacement(_, _, _)) =>
    }
  }

  it should "not allow overlapping BigStationPieces" in {
    val grid = MapGrid.empty(10, 10)
      .place(4, 4, BigStationPiece).toOption.get

    val overlappingResult = grid.place(5, 5, BigStationPiece)
    overlappingResult should matchPattern {
      case Left(PlacementError.InvalidPlacement(_, _, _)) =>
    }
  }

