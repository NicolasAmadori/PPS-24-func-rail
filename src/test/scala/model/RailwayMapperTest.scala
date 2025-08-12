package model

import model.mapgrid.*

import model.railway.Station.{bigStation, smallStation}
import model.railway.{MetalRail, Rail, Station, TitaniumRail}
import model.util.RailwayMapper
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RailwayMapperTest extends AnyFlatSpec with Matchers:

  // Helper values to make grid definitions more readable
  private val e: Cell = EmptyCell
  private val mr: Cell = MetalRailPiece()
  private val tr: Cell = TitaniumRailPiece()
  private def ss(id: Int): Cell = SmallStationPiece(id)
  private def bsc(id: Int): Cell = BigStationCenterPiece(id)
  private def bsb(id: Int): Cell = BigStationBorderPiece(id)

  /** Helper function to normalize a list of rails for comparison. This ignores the auto-generated rail 'code' and sorts
    * station endpoints (A, B) to make comparisons independent of processing order.
    *
    * @param rails
    *   the list of rails to normalize
    * @return
    *   a list of tuples with rail length, the stations connected and the type of the rail
    */
  private def normalizeRails(rails: List[Rail]): List[(Int, (String, String), RailType)] =
    rails.map { rail =>
      val stations = List(rail.stationA.toString, rail.stationB.toString).sorted
      val railType = rail match
        case _: MetalRail => MetalRailType
        case _: TitaniumRail => TitaniumRailType
      (rail.length, (stations.head, stations(1)), railType)
    }.sortBy(r => (r._2._1, r._2._2)).distinct // Sort by station pair for consistent order

  it should "return an empty railway from an empty grid" in {
    val grid = MapGrid(5, 5, Vector.fill(5)(Vector.fill(5)(e)))

    val railway = RailwayMapper.convert(grid)

    railway.stations should be(empty)
    railway.rails should be(empty)
  }

  it should "map a simple linear railway between two small stations" in {
    val grid = MapGrid(5, 1, Vector(Vector(ss(1), mr, mr, mr, ss(2))))

    val railway = RailwayMapper.convert(grid)

    val expectedStations = List(smallStation("ST1"), smallStation("ST2"))
    val expectedRails = List(
      (3, ("ST1", "ST2"), MetalRailType)
    )

    railway.stations should contain theSameElementsAs expectedStations
    normalizeRails(railway.rails) should contain theSameElementsAs expectedRails
  }

  it should "map a railway with a 90-degree turn" in {
    val cells = Vector(
      Vector(ss(1), mr, mr, e),
      Vector(e, e, mr, e),
      Vector(e, e, mr, ss(2))
    )
    val grid = MapGrid(4, 3, cells)

    val railway = RailwayMapper.convert(grid)

    val expectedStations = List(smallStation("ST1"), smallStation("ST2"))
    val expectedRails = List(
      (4, ("ST1", "ST2"), MetalRailType)
    )

    railway.stations should contain theSameElementsAs expectedStations
    normalizeRails(railway.rails) should contain theSameElementsAs expectedRails
  }

  it should "correctly map a big station connected to a small station" in {
    val cells = Vector(
      Vector(bsb(1), bsb(1), bsb(1), e, e),
      Vector(bsb(1), bsc(1), bsb(1), mr, ss(2)),
      Vector(bsb(1), bsb(1), bsb(1), e, e)
    )
    val grid = MapGrid(5, 3, cells)

    val railway = RailwayMapper.convert(grid)

    val expectedStations = List(bigStation("BST1"), smallStation("ST2"))
    val expectedRails = List(
      (1, ("BST1", "ST2"), MetalRailType)
    )

    railway.stations should contain theSameElementsAs expectedStations
    normalizeRails(railway.rails) should contain theSameElementsAs expectedRails
  }

  it should "map multiple rail types connecting different stations" in {
    // ST1 --metal-- ST2 --titanium-- ST3
    val cells = Vector(
      Vector(ss(1), mr, ss(2), tr, ss(3))
    )
    val grid = MapGrid(5, 1, cells)

    val railway = RailwayMapper.convert(grid)

    val expectedStations = List(smallStation("ST1"), smallStation("ST2"), smallStation("ST3"))
    val expectedRails = List(
      (1, ("ST1", "ST2"), MetalRailType),
      (1, ("ST2", "ST3"), TitaniumRailType)
    )

    railway.stations should contain theSameElementsAs expectedStations
    normalizeRails(railway.rails) should contain theSameElementsAs expectedRails
  }

  it should "map multiple rails originating from a single station (junction)" in {
    //      ST2
    //       |
    // ST1--ST3--ST4
    val cells = Vector(
      Vector(e, e, ss(2), e, e),
      Vector(e, e, mr, e, e),
      Vector(ss(1), mr, ss(3), mr, ss(4)),
      Vector(e, e, e, e, e)
    )
    // Manually update the grid to connect the vertical rail
    val gridWithJunction = MapGrid(5, 3, cells)
      .copy(cells = cells.updated(2, cells(2).updated(1, mr)))

    val railway = RailwayMapper.convert(gridWithJunction)

    val expectedStations = List(smallStation("ST1"), smallStation("ST2"), smallStation("ST3"), smallStation("ST4"))
    val expectedRails = List(
      (1, ("ST1", "ST3"), MetalRailType),
      (1, ("ST2", "ST3"), MetalRailType),
      (1, ("ST3", "ST4"), MetalRailType)
    )

    railway.stations should contain theSameElementsAs expectedStations
    normalizeRails(railway.rails) should contain theSameElementsAs expectedRails
  }

  it should "ignore rails that are dead-ends" in {
    // ST1 --- (rail to nowhere)
    val cells = Vector(Vector(ss(1), mr, mr, e))
    val grid = MapGrid(4, 1, cells)

    val railway = RailwayMapper.convert(grid)

    railway.stations should contain theSameElementsAs List(smallStation("ST1"))
    railway.rails should be(empty) // The dead-end rail should not be created
  }

  it should "not create a rail for a track that loops back to the same station" in {
    // ST1 -- ST1
    val cells = Vector(
      Vector(ss(1), mr, mr),
      Vector(mr, e, mr),
      Vector(mr, mr, mr)
    )
    val grid = MapGrid(3, 3, cells)

    val railway = RailwayMapper.convert(grid)

    railway.stations should contain theSameElementsAs List(smallStation("ST1"))
    // The rail is not valid because it doesn't connect to a *different* station
    railway.rails should be(empty)
  }

  it should "handle a complex grid with mixed station and rail types" in {
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

    val expectedStations = List(bigStation("BST1"), smallStation("ST2"), smallStation("ST3"))
    val expectedRails = List(
      (1, ("BST1", "ST2"), MetalRailType),
      (2, ("BST1", "ST3"), TitaniumRailType)
    )

    railway.stations should contain theSameElementsAs expectedStations
    normalizeRails(railway.rails) should contain theSameElementsAs expectedRails
  }
