package model.rail

import model.Domain.StationCode
import model.Rail.metalRail
import model.Station.{bigStation, smallStation}
import model.Train.{highSpeedTrain, normalTrain}
import model.{MetalRail, Station}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class RailTest extends AnyFlatSpec:

  val trainCode: Int = 1
  val railCode: Int = 1
  val railLength: Int = 10

  val stationA = "ST001"
  val stationB = "ST002"
  val stationC = "ST003"

  "A MetalRail" should "be created with code, length, stations" in {
    val rail = metalRail(railCode, railLength, stationA, stationB)
    rail.length should be(railLength)
    rail.stationA should be(StationCode.fromString(stationA))
    rail.stationB should be(StationCode.fromString(stationB))
    rail shouldBe a[MetalRail]
  }

  "A Rail" can "be connected to SmallStation or BigStation" in {
    val rail = metalRail(railCode, railLength, stationA, stationC)
    rail.stationA should be(StationCode.fromString(stationA))
    rail.stationB should be(StationCode.fromString(stationC))
  }

  "A MetalRail" should "accept a normal train and a high speed train" in {
    val rail = metalRail(railCode, railLength, stationA, stationB)
    rail.canAcceptTrain(normalTrain(trainCode)) should be(true)
    rail.canAcceptTrain(highSpeedTrain(trainCode)) should be(true)
  }
