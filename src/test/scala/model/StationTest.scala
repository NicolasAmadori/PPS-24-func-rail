package model

import model.Domain.StationCode
import model.Station.smallStation
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

class StationTest extends AnyFlatSpec:

  "A Station" should "be created with code" in {
    val station = smallStation("ST001")
    StationCode.value(station.code) should be("ST001")
  }
