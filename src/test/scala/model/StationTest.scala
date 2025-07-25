package model

import model.Domain.StationCode
import model.Station.smallStation
import model.Train.normalTrain
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class StationTest extends AnyFlatSpec:

  "A Station" should "be created with code" in {
    val station = smallStation("ST001")
    StationCode.value(station.code) should be("ST001")
  }

  it should "allow adding a train" in {
    val station = smallStation("ST001")
    val train = normalTrain(202)
    val stationWithTrains = station.addTrain(train)
    stationWithTrains.trains should contain(train)
  }

  it should "allow removing trains" in {
    val station = smallStation("ST001")
    val train1 = normalTrain(202)
    val train2 = normalTrain(203)
    val stationWithTrains = station.addTrain(train1).addTrain(train2)

    val stationAfterRemoval = stationWithTrains.removeTrain(train1)
    stationAfterRemoval.trains should not contain train1
    stationAfterRemoval.trains should contain(train2)
  }
