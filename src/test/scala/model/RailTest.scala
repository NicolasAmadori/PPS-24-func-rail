package model

import model.Domain.{RailCode, StationCode}
import model.Rail.{emptyMetalRail, emptyTitaniumRail, metalRail, titaniumRail}
import model.Station.{bigStation, smallStation}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class RailTest extends AnyFlatSpec:

  val stationA: Station = smallStation("ST001")
  val stationB: Station = smallStation("ST002")
  val stationC: Station = bigStation("ST003")

  "A MetalRail" should "be created with code, length, stations, train" in {
    val rail = metalRail(1, 10, stationA, stationB, Some(1))
    rail.length should be (10)
    rail.stationA shouldBe a [Station]
    rail.stationB shouldBe a [Station]
    rail shouldBe a [MetalRail]
  }

  "A TitaniumRail" should "be created with code, length, stations, train" in {
    val rail = titaniumRail(1, 10, stationA, stationB, Some(1))
    rail.length should be (10)
    rail.stationA shouldBe a [Station]
    rail.stationB shouldBe a [Station]
    rail shouldBe a [TitaniumRail]
  }

  "A Rail" can "be created with no train" in {
    val metalRail = emptyMetalRail(2, 20, stationA, stationB)
    val titaniumRail = emptyTitaniumRail(3, 30, stationA, stationC)
    metalRail.train should be (None)
    titaniumRail.train should be (None)
  }

  "A Rail" can "be connected to SmallStation or BigStation" in {
    val rail = emptyMetalRail(2, 20, stationA, stationC)
    rail.stationA.code should be (stationA.code)
    rail.stationB.code should be (stationC.code)
  }



