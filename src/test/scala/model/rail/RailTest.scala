package model.rail

import model.entities.EntityCodes.StationCode
import model.entities.MetalRail
import model.entities.Rail.metalRail
import model.entities.Train.{highSpeedTrain, normalTrain}
import model.entities.dsl.{buildHighSpeedTrain, buildNormalTrain}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*
import util.SampleRailway.SampleStation.*

class RailTest extends AnyFlatSpec:

  val trainCode: String = "1"
  val railCode: String = "MR1"
  val railLength: Int = 10

  "A MetalRail" should "be created with code, length, stations" in {
    val rail = metalRail(railCode, railLength, StationA, StationB)
    rail.length should be(railLength)
    rail.stationA should be(StationCode(StationA))
    rail.stationB should be(StationCode(StationB))
    rail shouldBe a[MetalRail]
  }

  it can "be connected to SmallStation or BigStation" in {
    val rail = metalRail(railCode, railLength, StationA, StationC)
    rail.stationA should be(StationCode(StationA))
    rail.stationB should be(StationCode(StationC))
  }

  it should "accept a normal train and a high speed train" in {
    val rail = metalRail(railCode, railLength, StationA, StationB)
    rail.canAcceptTrain(buildNormalTrain(trainCode):
      _ departsFrom StationCode(StationA) stopsAt StationCode(StationB)) should be(true)
    rail.canAcceptTrain(buildHighSpeedTrain(trainCode):
      _ departsFrom StationCode(StationA) stopsAt StationCode(StationB)) should be(true)
  }
