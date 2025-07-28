package model.rail

import model.railway.Domain.StationCode
import model.railway.Rail.titaniumRail
import model.simulation.Train.{highSpeedTrain, normalTrain}
import model.railway.TitaniumRail
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
    rail.canAcceptTrain(highSpeedTrain(trainCode, StationCode.listOf(stationA, stationB))) should be(true)
    rail.canAcceptTrain(normalTrain(trainCode, StationCode.listOf(stationA, stationB))) should be(false)
  }
