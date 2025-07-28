package model.simulation

import model.railway.Domain.StationCode

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class TrainRouteTest extends AnyFlatSpec:
  "A TrainRoute" should "be created with a valid route" in {
    val route = TrainRoute(StationCode.listOf("ST001", "ST002", "ST003"))
    route.fullRoute should be(StationCode.listOf("ST001", "ST002", "ST003"))
    route.currentStationIndex should be(0)
    route.forward should be(true)
  }
