package model.rail

import model.Rail.{emptyMetalRail, metalRail}
import model.Station.{bigStation, smallStation}
import model.Train.{highSpeedTrain, normalTrain}
import model.{MetalRail, Station}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

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

  "A MetalRail" can "be created with no train" in {
    val metalRail = emptyMetalRail(railCode, railLength, stationA, stationB)
    metalRail.train should be(None)
  }

  "A Rail" can "be connected to SmallStation or BigStation" in {
    val rail = emptyMetalRail(railCode, railLength, stationA, stationC)
    rail.stationA.code should be(stationA.code)
    rail.stationB.code should be(stationC.code)
  }

  "A MetalRail" should "accept a normal train and a high speed train" in {
    val rail = emptyMetalRail(railCode, railLength, stationA, stationB)
    rail.canAcceptTrain(normalTrain(trainCode)) should be(true)
    rail.canAcceptTrain(highSpeedTrain(trainCode)) should be(true)
  }

  "A Rail" should "be empty when no train is assigned" in {
    val rail = emptyMetalRail(railCode, railLength, stationA, stationB)
    rail.isEmpty should be(true)
  }

  "A Rail" should "not be empty when a train is assigned" in {
    val rail = metalRail(railCode, railLength, stationA, stationB, normalTrain(trainCode))
    rail.isEmpty should be(false)
  }

  "A Rail" should "not accept a train if there's another one" in {
    val rail = metalRail(railCode, railLength, stationA, stationB, normalTrain(trainCode))
    rail.canAcceptTrain(normalTrain(trainCode)) should be(false)
  }
