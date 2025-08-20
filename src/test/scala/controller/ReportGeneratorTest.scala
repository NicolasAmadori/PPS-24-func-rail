package controller

import controller.simulation.ReportGenerator
import model.entities.EntityCodes.{RailCode, StationCode, TrainCode}
import model.entities.{Itinerary, ItineraryLeg}
import model.entities.Rail.metalRail
import model.entities.Station.smallStation
import model.entities.Train.normalTrain
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

  it should "retrieve all most used rails if there's a tie" in {
    val rail1 = metalRail(1, 100, StationA, StationB)
    val rail2 = metalRail(2, 200, StationB, StationC)
    val rail3 = metalRail(3, 300, StationC, StationD)
    val rail4 = metalRail(4, 400, StationD, StationA)
    val route1 = List(rail1, rail2, rail3)
    val route2 = List(rail4, rail3, rail2, rail1)
    val mostUsedRails = ReportGenerator.mostUsedRails(List(route1, route2).flatten)
    mostUsedRails.rails.map(_.code) should contain allOf (RailCode(1), RailCode(2), RailCode(3))
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

  it should "retrieve the most used train" in {
    val (stationA, stationB, stationC) = (StationCode(StationA), StationCode(StationB), StationCode(StationC))
    val train1 = normalTrain("T1", List(stationA, stationB))
    val train2 = normalTrain("T2", List(stationB, stationC))
    val itineraries = List(
      Itinerary(List(
        ItineraryLeg(train1, stationA, stationB)
      )),
      Itinerary(List(
        ItineraryLeg(train1, stationA, stationB),
        ItineraryLeg(train2, stationB, stationC)
      ))
    )

    val mostUsedTrains = ReportGenerator.mostUsedTrain(itineraries)
    mostUsedTrains.trains should be(List(TrainCode("T1")))
  }

  it should "retrieve all the most used train if there's a tie" in {
    val (stationA, stationB, stationC) = (StationCode(StationA), StationCode(StationB), StationCode(StationC))
    val train1 = normalTrain("T1", List(stationA, stationB))
    val train2 = normalTrain("T2", List(stationB, stationC))
    val itinerary = Itinerary(List(
      ItineraryLeg(train1, stationA, stationB),
      ItineraryLeg(train2, stationB, stationC)
    ))

    val mostUsedTrains = ReportGenerator.mostUsedTrain(List(itinerary, itinerary))
    mostUsedTrains.trains should contain allOf (TrainCode("T1"), TrainCode("T2"))
  }
