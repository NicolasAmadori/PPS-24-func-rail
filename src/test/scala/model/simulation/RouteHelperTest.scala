package model.simulation

import model.railway.Domain.StationCode
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
      railway,
      StationCode(StationB),
      stations.filterNot(_.value == StationB)
    )

    val stationsInResult = result.get.flatMap(r => List(r.stationA, r.stationB)).toSet
    stations.foreach(s => stationsInResult should contain(s))
  }

  it should "be empty if there is no path" in {
    val railway = SampleRailway.railway2
    val stations = List(StationB, StationD, StationE).map(StationCode(_))
    val result = RouteHelper.getRouteForTrain(
      normalTrain(trainCode, stations),
      railway,
      StationCode(StationB),
      stations.filterNot(_.value == StationB)
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
      railway,
      StationCode(StationB),
      stations.filterNot(_.value == StationB)
    )
    val resultForHighSpeed = RouteHelper.getRouteForTrain(
      highSpeed,
      railway,
      StationCode(StationB),
      stations.filterNot(_.value == StationB)
    )
    resultForNormal should be(None)
    val stationsInResult = resultForHighSpeed.get.flatMap(r => List(r.stationA, r.stationB)).toSet
    stations.foreach(s => stationsInResult should contain(s))

  }
