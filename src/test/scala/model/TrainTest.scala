package model

import model.entities.EntityCodes.{StationCode, TrainCode}
import model.entities.Rail.{metalRail, titaniumRail}
import model.entities.{Route, Train}
import model.entities.Train.{defaultSpeed, highSpeed, highSpeedTrain, normalTrain}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class TrainTest extends AnyFlatSpec:

  val trainCode1 = "101"
  val trainCode2 = "202"

  val stationCode1 = "ST001"
  val stationCode2 = "ST002"
  val stationCode3 = "ST003"
  val stops = StationCode.listOf(stationCode1, stationCode2, stationCode3)

  "A Train" should "be created with code and stops" in {
    val train = normalTrain(trainCode1, StationCode.listOf(stationCode1, stationCode2))
    train.code should be(TrainCode(trainCode1))
    train.code.value should be(trainCode1)
    train.speed should be(defaultSpeed)
    train.stations should be(List(stationCode1, stationCode2))
    train.route.isEmpty should be(true)

    val fastTrain = highSpeedTrain(trainCode2, StationCode.listOf(stationCode2, stationCode3))
    fastTrain.code should be(TrainCode(trainCode2))
    fastTrain.code.value should be(trainCode2)
    fastTrain.speed should be(highSpeed)
    fastTrain.stations should be(List(stationCode2, stationCode3))
    fastTrain.route.isEmpty should be(true)
  }

  it should "not allow duplicate stations in the stops" in {
    val route = StationCode.listOf(stationCode1, stationCode2, stationCode1)
    assertThrows[IllegalArgumentException]:
      normalTrain(trainCode1, route)
  }

  it should "not allow empty stops" in {
    assertThrows[IllegalArgumentException]:
      normalTrain(trainCode1, List.empty)
  }

  it should "allow adding a route" in {
    val railLength = 10
    val train = normalTrain(trainCode1, stops)
    val trainWithRoute = train.withRoute(Route(List(
      metalRail("MR1", railLength, stationCode1, stationCode2),
      metalRail("MR2", railLength, stationCode2, stationCode3)
    )))

    trainWithRoute.route.railsCount should be(2)
    trainWithRoute.route.length should be(railLength + railLength)
    trainWithRoute.route.isEmpty should be(false)
  }

  "Train's travel time on a rail" should "be computed based on its speed" in {
    val metal = metalRail("MR1", 1000, stationCode1, stationCode2)
    val train = normalTrain(trainCode1, stops)
    val fastTrain = highSpeedTrain(trainCode2, stops)
    val trainTime = train.getTravelTime(metal)
    val fastTrainTime = fastTrain.getTravelTime(metal)

    trainTime should be(math.ceil(metal.length / train.speed))
    fastTrainTime should be(math.ceil(metal.length / Train.defaultSpeed))
    fastTrainTime should be(trainTime)
  }

  "Fast train's travel time" should "be greater on metal rails when of the same length of a titanium" in {
    val fastTrain = highSpeedTrain(trainCode2, stops)
    val metal = metalRail("MR1", 1000, stationCode1, stationCode2)
    val titanium = titaniumRail("TR2", 1000, stationCode1, stationCode2)

    fastTrain.getTravelTime(titanium) should be < (fastTrain.getTravelTime(metal))
  }
