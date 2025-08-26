package model

import model.entities.EntityCodes.StationCode
import model.entities.{Itinerary, NormalTrain}
import model.entities.dsl.ItineraryDSL.leg
import model.entities.Train.normalTrain
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.shouldBe

class ItineraryDslTest extends AnyFlatSpec:

  private val stationA = StationCode("S1")
  private val stationB = StationCode("S2")
  private val stationC = StationCode("S3")

  private val train: NormalTrain = normalTrain("101", List(stationA, stationB, stationC))

  "Itinerary DSL" should "create an itinerary with correct details" in {
    val itineraryLeg1 = leg(train) from stationA to stationB
    val itineraryLeg2 = leg(train) from stationB to stationC
    val itinerary = Itinerary(List(itineraryLeg1, itineraryLeg2))

    itinerary.start shouldBe stationA
    itinerary.end shouldBe stationC
    itinerary.legs.size shouldBe (2)
    itinerary.legs.head.from shouldBe stationA
    itinerary.legs.head.to shouldBe stationB
    itinerary.legs.last.from shouldBe stationB
    itinerary.legs.last.to shouldBe stationC
  }
