package controller

import controller.simulation.ReportGenerator
import model.entities.EntityCodes.{RailCode, StationCode}
import model.entities.Rail.metalRail
import model.simulation.TrainPosition.{AtStation, OnRail}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*
import util.SampleRailway
import util.SampleRailway.SampleStation.*

class ReportGeneratorTest extends AnyFlatSpec:
  "Report generator" should "retrieve most used rail" in {
    val rail1 = metalRail(1, 100, StationA, StationB)
    val rail2 = metalRail(2, 200, StationB, StationC)
    val rail3 = metalRail(3, 300, StationC, StationD)
    val rail4 = metalRail(4, 400, StationD, StationA)
    val route1 = List(rail1, rail2, rail3, rail4, rail1)
    val route2 = List(rail4, rail3, rail2, rail1)
    val mostUsedRails = ReportGenerator.mostUsedRails(List(route1, route2).flatten)
    mostUsedRails.rails.map(_.code) should contain(RailCode(1))
    mostUsedRails.getMeasurementUnit should be("")
    mostUsedRails.toString should be("Most used rails")
  }

  it should "retrieve average waiting time" in {
    val positions1 = List(
      AtStation(StationCode(StationA)),
      OnRail(RailCode(1)),
      OnRail(RailCode(1)),
      OnRail(RailCode(1)),
      AtStation(StationCode(StationB)),
      AtStation(StationCode(StationB))
    )
    val positions2 = List(
      AtStation(StationCode(StationA)),
      AtStation(StationCode(StationA)),
      AtStation(StationCode(StationA)),
      OnRail(RailCode(1)),
      OnRail(RailCode(1)),
      OnRail(RailCode(1)),
      AtStation(StationCode(StationB))
    )
    val averageWaiting = ReportGenerator.averageTrainWaiting(List(positions1, positions2))
    averageWaiting.hours should be(1.5)
    averageWaiting.getMeasurementUnit should be("hours")
    averageWaiting.toString should be("Average train waiting")
  }
