package model

import model.Domain.StationCode
import model.Station.{bigStation, smallStation}
import model.Train.{highSpeedTrain, normalTrain}
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

  it should "allow adding a train" in {
    val station = smallStation(stationCode)
    val train = normalTrain(trainCode1)
    val stationWithTrains = station.addTrain(train)
    stationWithTrains.trains should contain(train)
  }

  it should "allow removing trains" in {
    val station = bigStation(stationCode)
    val train1 = normalTrain(trainCode1)
    val train2 = highSpeedTrain(trainCode2)
    val stationWithTrains = station.addTrain(train1).addTrain(train2)

    val stationAfterRemoval = stationWithTrains.removeTrain(train1)
    stationAfterRemoval.trains should not contain train1
    stationAfterRemoval.trains should contain(train2)
  }

  it should "not add the same train twice" in {
    val station = bigStation(stationCode)
    val train = normalTrain(trainCode1)
    val stationWithTrains = station.addTrain(train).addTrain(train)
    stationWithTrains.trains.count(_ == train) should be(1)
  }

  it should "not remove a train that does not exist" in {
    val station = smallStation(stationCode)
    val train = normalTrain(trainCode1)
    val stationWithTrains = station.addTrain(train)

    val stationAfterRemoval = stationWithTrains.removeTrain(highSpeedTrain(trainCode2))
    stationAfterRemoval.trains should contain(train)
  }
