package model.rail

import model.Domain.StationCode
import model.Rail.titaniumRail
import model.Train.{highSpeedTrain, normalTrain}
import model.TitaniumRail
import org.scalatest.matchers.should.Matchers.*

class TitaniumRailTest extends RailTest:

  "A TitaniumRail" should "be created with code, length, stations, high speed train" in {
    val rail = titaniumRail(railCode, railLength, stationA, stationB)
    rail.length should be(railLength)
    rail.stationA should be(StationCode.fromString(stationA))
    rail.stationB should be(StationCode.fromString(stationB))
    rail shouldBe a[TitaniumRail]
  }

  "A TitaniumRail" should "accept only high speed trains" in {
    val rail = titaniumRail(railCode, railLength, stationA, stationB)
    rail.canAcceptTrain(highSpeedTrain(trainCode)) should be(true)
    rail.canAcceptTrain(normalTrain(trainCode)) should be(false)
  }
