package model.rail

import model.entities.EntityCodes.StationCode
import model.entities.Rail.titaniumRail
import model.entities.TitaniumRail
import model.entities.Train.{highSpeedTrain, normalTrain}
import org.scalatest.matchers.should.Matchers.*

class TitaniumRailTest extends RailTest:

  "A TitaniumRail" should "be created with code, length, stations, high speed train" in {
    val rail = titaniumRail(railCode, railLength, stationA, stationB)
    rail.length should be(railLength)
    rail.stationA should be(StationCode(stationA))
    rail.stationB should be(StationCode(stationB))
    rail shouldBe a[TitaniumRail]
  }

  "A TitaniumRail" should "accept only high speed trains" in {
    val rail = titaniumRail(railCode, railLength, stationA, stationB)
    rail.canAcceptTrain(highSpeedTrain(trainCode, StationCode.listOf(stationA, stationB))) should be(true)
    rail.canAcceptTrain(normalTrain(trainCode, StationCode.listOf(stationA, stationB))) should be(false)
  }
