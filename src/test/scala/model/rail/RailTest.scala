package model.rail

import model.railway.Domain.StationCode
import model.railway.Rail.metalRail

import model.simulation.Train.{highSpeedTrain, normalTrain}
import model.railway.MetalRail
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class RailTest extends AnyFlatSpec:

  val trainCode: String = "1"
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

  it can "be connected to SmallStation or BigStation" in {
    val rail = metalRail(railCode, railLength, stationA, stationC)
    rail.stationA should be(StationCode.fromString(stationA))
    rail.stationB should be(StationCode.fromString(stationC))
  }

  it should "accept a normal train and a high speed train" in {
    val rail = metalRail(railCode, railLength, stationA, stationB)
    rail.canAcceptTrain(normalTrain(trainCode, StationCode.listOf(stationA, stationB))) should be(true)
    rail.canAcceptTrain(highSpeedTrain(trainCode, StationCode.listOf(stationA, stationB))) should be(true)
  }
