package model

import model.mapgrid.*
import model.railway.RailwayPrologChecker
import model.util.RailwayMapper
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RailwayPrologCheckerTest extends AnyFlatSpec with Matchers:
  // Helper values to make grid definitions more readable
  private val e: Cell = EmptyCell
  private val mr: Cell = MetalRailPiece()
  private val tr: Cell = TitaniumRailPiece()

  private def ss(id: Int): Cell = SmallStationPiece(id)

  private def bsc(id: Int): Cell = BigStationCenterPiece(id)

  private def bsb(id: Int): Cell = BigStationBorderPiece(id)

  it should "return true if the railway is connected" in {
    // BST1 ---metal--- ST2
    //          |
    //       titanium
    //          |
    //         ST3
    val cells = Vector(
      Vector(bsb(1), bsb(1), bsb(1), e, e),
      Vector(bsb(1), bsc(1), bsb(1), mr, ss(2)),
      Vector(bsb(1), bsb(1), bsb(1), e, e),
      Vector(e, e, tr, e, e),
      Vector(e, e, tr, ss(3), e)
    )
    val grid = MapGrid(5, 5, cells)

    val railway = RailwayMapper.convert(grid)

    val isConnected = RailwayPrologChecker.isRailwayConnected(railway)
    isConnected should be(true)
  }

  it should "return false if the railway is not connected" in {
    // BST1 ---metal--- ST2
    //          |
    //       titanium
    //          |
    // ST4     ST3
    val cells = Vector(
      Vector(bsb(1), bsb(1), bsb(1), e, e),
      Vector(bsb(1), bsc(1), bsb(1), mr, ss(2)),
      Vector(bsb(1), bsb(1), bsb(1), e, e),
      Vector(e, e, tr, e, e),
      Vector(ss(4), e, tr, ss(3), e)
    )
    val grid = MapGrid(5, 5, cells)

    val railway = RailwayMapper.convert(grid)

    val isConnected = RailwayPrologChecker.isRailwayConnected(railway)
    isConnected should be(false)
  }
