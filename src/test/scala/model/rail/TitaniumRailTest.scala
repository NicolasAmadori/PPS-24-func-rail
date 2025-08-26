package model.rail

import model.entities.EntityCodes.StationCode
import model.entities.Rail.titaniumRail
import model.entities.TitaniumRail
import model.entities.Train.{highSpeedTrain, normalTrain}
import org.scalatest.matchers.should.Matchers.*
import util.SampleRailway.SampleStation.*

class TitaniumRailTest extends RailTest:

  "A TitaniumRail" should "be created with code, length, stations, high speed train" in {
    val rail = titaniumRail(railCode, railLength, StationA, StationB)
    rail.length should be(railLength)
    rail.stationA should be(StationCode(StationA))
    rail.stationB should be(StationCode(StationB))
    rail shouldBe a[TitaniumRail]
  }

  "A TitaniumRail" should "accept only high speed trains" in {
    val rail = titaniumRail(railCode, railLength, StationA, StationB)
    rail.canAcceptTrain(highSpeedTrain(trainCode, StationCode.listOf(StationA, StationB))) should be(true)
    rail.canAcceptTrain(normalTrain(trainCode, StationCode.listOf(StationA, StationB))) should be(false)
  }
