package model.simulation

import model.railway.Domain.StationCode
import model.railway.Rail.metalRail
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class TrainRouteTest extends AnyFlatSpec:

  private val rails = List(
    metalRail(1, 15, "ST001", "ST002"),
    metalRail(2, 30, "ST002", "ST003"),
    metalRail(3, 45, "ST003", "ST004")
  )

  "A TrainRoute" should "be created with a valid route" in {
    val route = TrainRoute(rails)
    route.fullRoute should be(rails)
    route.currentRailIndex should be(0)
    route.forward should be(true)
  }
