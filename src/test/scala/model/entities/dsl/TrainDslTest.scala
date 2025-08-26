package model.entities.dsl

import model.entities.EntityCodes.StationCode
import model.entities.dsl.TrainType.{HighSpeed, Normal}
import model.entities.dsl.train
import model.entities.{HighSpeedTrain, NormalTrain, Route}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*
import util.SampleRailway
import util.SampleRailway.SampleStation.*

class TrainDslTest extends AnyFlatSpec:
  private val trainCode = "T1"
  private val railway = SampleRailway.railway1
  private val stationCodeA = StationCode(StationA)
  private val stationCodeB = StationCode(StationB)
  private val stationCodeC = StationCode(StationC)

  "Train builder" should "allow creating a train with name, normal type, departure and stops" in {
    val newTrain = train(trainCode):
      _ ofType Normal departsFrom stationCodeA stopsAt (stationCodeB, stationCodeC)

    newTrain.departureStation should be(stationCodeA)
    newTrain.isInstanceOf[NormalTrain] should be(true)
    newTrain.route should be(Route.empty)
  }

  it should "allow creating a train with name, high speed type, departure and stops" in {
    val newTrain = train(trainCode):
      _ ofType HighSpeed departsFrom stationCodeA stopsAt (stationCodeB, stationCodeC)

    newTrain.departureStation should be(stationCodeA)
    newTrain.isInstanceOf[HighSpeedTrain] should be(true)
    newTrain.route should be(Route.empty)
  }

  it should "allow setting a railway on which compute the route" in {
    val newTrain = train(trainCode):
      _ ofType Normal in railway departsFrom stationCodeA stopsAt (stationCodeB, stationCodeC)

    newTrain.route should not be (Route.empty)
  }

  it should "not allow creating an untyped train" in {
    assertThrows[IllegalStateException]:
      train(trainCode):
        _ departsFrom stationCodeB stopsAt stationCodeC
  }

  it should "not allow creating a train without departure station" in {
    assertThrows[IllegalStateException]:
      train(trainCode):
        _ ofType HighSpeed stopsAt stationCodeC
  }

  it should "not allow creating a train without stops" in {
    assertThrows[IllegalStateException]:
      train(trainCode):
        _ ofType HighSpeed departsFrom stationCodeC
  }

  it should "not allow creating a train without at least two distinct stations" in {
    assertThrows[IllegalStateException]:
      train(trainCode):
        _ ofType HighSpeed departsFrom stationCodeC stopsAt stationCodeC
  }
