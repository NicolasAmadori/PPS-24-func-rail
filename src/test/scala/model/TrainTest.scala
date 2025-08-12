package model

import model.entities.EntityCodes.{StationCode, TrainCode}
import model.entities.Train.{highSpeed, highSpeedTrain, defaultSpeed, normalTrain}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class TrainTest extends AnyFlatSpec:

  val trainCode1 = "101"
  val trainCode2 = "202"

  val stationCode1 = "ST001"
  val stationCode2 = "ST002"
  val stationCode3 = "ST003"

  "A Train" should "be created with code" in {
    val train = normalTrain(trainCode1, StationCode.listOf(stationCode1, stationCode2))
    train.code should be(TrainCode(trainCode1))
    train.speed should be(defaultSpeed)

    val fastTrain = highSpeedTrain(trainCode2, StationCode.listOf(stationCode2, stationCode3))
    fastTrain.code should be(TrainCode(trainCode2))
    fastTrain.speed should be(highSpeed)
  }

  it should "allow setting a route" in {
    val route = StationCode.listOf(stationCode1, stationCode2, stationCode3)
    val train = normalTrain(trainCode1, route)

    train.stations should be(route)
  }

  it should "not allow duplicate stations in the route" in {
    val route = StationCode.listOf(stationCode1, stationCode2, stationCode1)
    assertThrows[IllegalArgumentException]:
      normalTrain(trainCode1, route)
  }

  it should "not allow empty routes" in {
    assertThrows[IllegalArgumentException]:
      normalTrain(trainCode1, List.empty)
  }
