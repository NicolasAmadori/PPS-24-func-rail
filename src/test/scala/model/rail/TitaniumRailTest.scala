package model.rail

import model.Rail.{emptyMetalRail, emptyTitaniumRail, titaniumRail}
import model.Train.{highSpeedTrain, normalTrain}
import model.{Station, TitaniumRail}
import org.scalatest.matchers.should.Matchers.*

class TitaniumRailTest extends RailTest:

  "A TitaniumRail" should "be created with code, length, stations, high speed train" in {
    val rail = titaniumRail(railCode, railLength, stationA, stationB, highSpeedTrain(trainCode))
    rail.length should be(railLength)
    rail.train should be(Some(highSpeedTrain(trainCode)))
    rail.stationA shouldBe a[Station]
    rail.stationB shouldBe a[Station]
    rail shouldBe a[TitaniumRail]
  }

  "A TitaniumRail" can "be created with no train" in {
    val titaniumRail = emptyTitaniumRail(railCode, railLength, stationA, stationC)
    titaniumRail.train should be(None)
  }

  "A TitaniumRail" should "accept only high speed trains" in {
    val rail = emptyTitaniumRail(railCode, railLength, stationA, stationB)
    rail.canAcceptTrain(highSpeedTrain(trainCode)) should be(true)
    rail.canAcceptTrain(normalTrain(trainCode)) should be(false)
  }
