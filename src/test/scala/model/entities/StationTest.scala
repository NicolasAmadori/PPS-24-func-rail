package model.entities

import model.entities.EntityCodes.StationCode
import model.entities.Station.smallStation
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class StationTest extends AnyFlatSpec:

  val stationCode = "ST001"
  val trainCode1 = 202
  val trainCode2 = 203

  "A Station" should "be created with code" in {
    val station = smallStation(stationCode)
    StationCode.value(station.code) should be(stationCode)
  }
