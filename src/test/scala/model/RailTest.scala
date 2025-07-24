package model

import model.Rail.emptyMetalRail
import model.Rail.emptyTitaniumRail
import model.Rail.metalRail
import model.Rail.titaniumRail
import model.Station.bigStation
import model.Station.smallStation
import model.Train.highSpeedTrain
import model.Train.normalTrain
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

class RailTest extends AnyFlatSpec:

  val trainCode: Int = 1
  val railCode: Int = 1
  val railLength: Int = 10

  val stationA: Station = smallStation("ST001")
  val stationB: Station = smallStation("ST002")
  val stationC: Station = bigStation("ST003")

  "A MetalRail" should "be created with code, length, stations, normal train" in {
    val rail = metalRail(railCode, railLength, stationA, stationB, normalTrain(trainCode))
    rail.length should be(railLength)
    rail.train should be(Some(normalTrain(trainCode)))
    rail.stationA shouldBe a[Station]
    rail.stationB shouldBe a[Station]
    rail shouldBe a[MetalRail]
  }

  "A MetalRail" should "be created with code, length, stations, high speed train" in {
    val rail = metalRail(railCode, railLength, stationA, stationB, highSpeedTrain(trainCode))
    rail.train should be(Some(highSpeedTrain(trainCode)))
    rail shouldBe a[MetalRail]
  }

  "A TitaniumRail" should "be created with code, length, stations, high speed train" in {
    val rail = titaniumRail(railCode, railLength, stationA, stationB, highSpeedTrain(trainCode))
    rail.length should be(railLength)
    rail.train should be(Some(highSpeedTrain(trainCode)))
    rail.stationA shouldBe a[Station]
    rail.stationB shouldBe a[Station]
    rail shouldBe a[TitaniumRail]
  }

  "A Rail" can "be created with no train" in {
    val metalRail = emptyMetalRail(railCode, railLength, stationA, stationB)
    val titaniumRail = emptyTitaniumRail(railCode, railLength, stationA, stationC)
    metalRail.train should be(None)
    titaniumRail.train should be(None)
  }

  "A Rail" can "be connected to SmallStation or BigStation" in {
    val rail = emptyMetalRail(railCode, railLength, stationA, stationC)
    rail.stationA.code should be(stationA.code)
    rail.stationB.code should be(stationC.code)
  }
