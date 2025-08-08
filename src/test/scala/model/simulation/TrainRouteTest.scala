package model.simulation

import model.railway.Domain.StationCode
import model.railway.Rail.metalRail
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class TrainRouteTest extends AnyFlatSpec:

  private val stations = List(
    "ST001",
    "ST002",
    "ST003",
    "ST004"
  ).map(StationCode(_))

  private val rails = List(
    metalRail(1, 15, "ST001", "ST002"),
    metalRail(2, 30, "ST002", "ST003"),
    metalRail(3, 45, "ST003", "ST004")
  )

  "A TrainRoute" should "be created with a valid route" in {
    val route = TrainRoute(rails, stations)
    route.fullRoute should be(rails)
    route.currentRailIndex should be(0)
    route.forward should be(true)
  }

  it should "throw an exception if stops are not part of the route" in {
    val invalidStations = List("ST001", "ST005").map(StationCode(_))
    assertThrows[IllegalArgumentException] {
      TrainRoute(rails, invalidStations)
    }
  }
