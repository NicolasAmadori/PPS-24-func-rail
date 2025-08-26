package model

import model.entities.EntityCodes.{PassengerCode, StationCode}
import model.entities.dsl.ItineraryDSL.leg
import model.entities.Train.normalTrain
import model.entities.{Itinerary, NormalTrain, Passenger}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class PassengerDslTest extends AnyFlatSpec:

  private val stationA = StationCode("S1")
  private val stationB = StationCode("S2")

  val stationCode1: String = "ST001"
  val stationCode2: String = "ST002"
  val trainCode1 = "101"

  private val train: NormalTrain = normalTrain(trainCode1, StationCode.listOf(stationCode1, stationCode2))
  private val itineraryLeg = leg(train) from StationCode(stationCode1) to StationCode(stationCode2)
  private val itinerary = Itinerary(List(itineraryLeg))

  "Passenger DSL" should "create a passenger with departure, arrival and itinerary" in {
    val passenger = Passenger("P1")
      .from(stationA)
      .to(stationB)
      .withItinerary(itinerary)

    passenger.code shouldBe PassengerCode("P1")
    passenger.departure shouldBe stationA
    passenger.destination shouldBe stationB
    passenger.itinerary shouldBe Some(itinerary)
  }

  it should "create a passenger with no itinerary" in {
    val passenger = Passenger("P2")
      .from(stationA)
      .to(stationB)
      .withNoItinerary

    passenger.code shouldBe PassengerCode("P2")
    passenger.departure shouldBe stationA
    passenger.destination shouldBe stationB
    passenger.itinerary shouldBe None
  }

  it should "allow chaining in different order (from -> to -> withNoItinerary)" in {
    val passenger = Passenger("P3")
      .from(stationA)
      .to(stationB)
      .withNoItinerary

    passenger.departure shouldBe stationA
    passenger.destination shouldBe stationB
    passenger.itinerary shouldBe None
  }

  it should "not mutate previous passengers when chaining" in {
    val base = Passenger("P4")
    val p1 = base.from(stationA).to(stationB).withNoItinerary
    val p2 = base.from(stationB).to(stationA).withItinerary(itinerary)

    p1.departure shouldBe stationA
    p1.destination shouldBe stationB
    p1.itinerary shouldBe None

    p2.departure shouldBe stationB
    p2.destination shouldBe stationA
    p2.itinerary shouldBe Some(itinerary)
  }
