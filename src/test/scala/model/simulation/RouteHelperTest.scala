package model.simulation

import model.railway.EntityCodes.StationCode
import model.simulation.Train.{highSpeedTrain, normalTrain}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*
import utils.SampleRailway
import SampleRailway.SampleStation.*

class RouteHelperTest extends AnyFlatSpec:

  private val trainCode = "T1"

  "The route" should "contain all the stops" in {
    val railway = SampleRailway.railway1
    val stations = List(StationB, StationD, StationE).map(StationCode(_))
    val result = RouteHelper.getRouteForTrain(
      normalTrain(trainCode, stations),
      railway
    )

    result match
      case Some(r) =>
        stations.foreach(s => r.stations should contain(s))
      case _ => fail("Test should retrieve some result")
  }

  it should "be empty if there is no path" in {
    val railway = SampleRailway.railway2
    val stations = List(StationB, StationD, StationE).map(StationCode(_))
    val result = RouteHelper.getRouteForTrain(
      normalTrain(trainCode, stations),
      railway
    )

    result should be(None)
  }

  it can "be different for trains of different types" in {
    val railway = SampleRailway.railway2
    val stations = List(StationB, StationD, StationE).map(StationCode(_))
    val normal = normalTrain(trainCode, stations)
    val highSpeed = highSpeedTrain(trainCode, stations)
    val resultForNormal = RouteHelper.getRouteForTrain(
      normal,
      railway
    )
    val resultForHighSpeed = RouteHelper.getRouteForTrain(
      highSpeed,
      railway
    )
    resultForNormal should be(None)
    resultForHighSpeed match
      case Some(r) =>
        stations.foreach(s => r.stations should contain(s))
      case _ => fail("High speed trains should retrieve some result")
  }
