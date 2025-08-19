package model.simulation

import controller.simconfig.{SimulationBuilder, SimulationFormState}
import model.entities.EntityCodes.StationCode
import model.entities.Rail.metalRail
import model.entities.{HighSpeedTrain, NormalTrain, Passenger, PassengerPosition, Station, Train}
import model.entities.Train.{highSpeedTrain, normalTrain}
import model.railway.Railway
import model.util.PassengerGenerator
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class SimulationPassengerUpdateTest extends AnyFlatSpec:

  val stationCode1 = "ST001"
  val stationCode2 = "ST002"
  val stationCode3 = "ST003"
  val rail1 = 1
  val rail2 = 2
  val stationCodes = List(stationCode1, stationCode2, stationCode3)

  val simulationDuration = 100

  val trainCode1 = "101"
  val trainCode2 = "202"
  val train1: NormalTrain = normalTrain(trainCode1, StationCode.listOf(stationCode1, stationCode2))
  val train2: HighSpeedTrain = highSpeedTrain(trainCode2, StationCode.listOf(stationCode2, stationCode3))

  val railway: Railway = Railway.withStations(
    List(
      Station.smallStation(stationCode1),
      Station.bigStation(stationCode2),
      Station.smallStation(stationCode3)
    )
  ).withRails(
    List(
      metalRail(rail1, 200, stationCode1, stationCode2),
      metalRail(rail2, 500, stationCode2, stationCode3)
    )
  )

  private def setupSingleTrainScenario: SimulationFormState =
    var localState = SimulationFormState()
    val (id1, newForm1) = localState.addTrain()
    localState = newForm1
    localState = localState.setDepartureStation(id1, StationCode(stationCode1))
    localState = localState.addStop(id1, StationCode(stationCode2))
    localState = localState.updateTrainName(id1, "Primo")
    localState

  private def setupDoubleTrainScenario: SimulationFormState =
    var localState = setupSingleTrainScenario
    val (id2, newForm2) = localState.addTrain()
    localState = newForm2
    localState = localState.setDepartureStation(id2, StationCode(stationCode3))
    localState = localState.addStop(id2, StationCode(stationCode2))
    localState = localState.updateTrainName(id2, "Secondo")
    localState

  "Passengers" should "not be present initially" in {
    val state = SimulationState(List(train1, train2))

    state.passengers should be(empty)
  }

  "Passengers" should "be generated and should be in a station" in {
    val PLAYER_NUMBER = 5
    val state = SimulationState(List(train1, train2))
    val passengerGenerator = PassengerGenerator(railway, state.trains)
    val (newState, newGenerator, logs) = state.generatePassengers(passengerGenerator)(PLAYER_NUMBER)

    newState.passengers.size should be(PLAYER_NUMBER)
    newState.passengerStates.size should be(PLAYER_NUMBER)
    logs.size should be(PLAYER_NUMBER)

    newState.passengerStates.foreach { (_, state) =>
      assert(state.currentPosition match
        case PassengerPosition.AtStation(station) => stationCodes.contains(station)
        case PassengerPosition.OnTrain(_) => false)
    }
  }

  "Passengers" should "get on a train if possible" in {
    val localState = setupDoubleTrainScenario

    val result = SimulationBuilder.build(100, railway, localState.trains)

    result.isRight should be(true)

    var sim = result.toOption.get

    var selectedPassenger1: Option[Passenger] = None
    var selectedPassenger2: Option[Passenger] = None

    while selectedPassenger1.isEmpty || selectedPassenger2.isEmpty do
      val (s1, l) = sim.start()
      sim = s1
      selectedPassenger1 = sim.state.passengers.find(p =>
        p.itinerary.isDefined
          && p.itinerary.get.start == StationCode(stationCode2)
          && p.itinerary.get.end == StationCode(stationCode1)
      )
      selectedPassenger2 = sim.state.passengers.find(p =>
        p.itinerary.isDefined
          && p.itinerary.get.start == StationCode(stationCode2)
          && p.itinerary.get.end == StationCode(stationCode3)
      )

    val N_STEPS = 20

    (1 to N_STEPS + 1).foreach(n =>
      val res = sim.doStep()
      res.isRight should be(true)
      val (s, l) = res.toOption.get
      sim = s
    )

    // Assert that the selected passenger arrived at its destination
    sim.state.passengerStates
      .find((pCode, _) =>
        selectedPassenger1.get.code == pCode
      ).get._2.currentPosition shouldBe PassengerPosition.AtStation(selectedPassenger1.get.itinerary.get.end)

    sim.state.passengerStates
      .find((pCode, _) =>
        selectedPassenger2.get.code == pCode
      ).get._2.currentPosition shouldBe PassengerPosition.AtStation(selectedPassenger2.get.itinerary.get.end)
  }

  "Passengers" should "change train if needed" in {
    val localState = setupDoubleTrainScenario

    val result = SimulationBuilder.build(100, railway, localState.trains)

    result.isRight should be(true)

    var sim = result.toOption.get

    var selectedPassenger: Option[Passenger] = None

    while selectedPassenger.isEmpty do
      val (s1, l) = sim.start()
      selectedPassenger = s1.state.passengers.find(p =>
        p.itinerary.isDefined
          && p.itinerary.get.start == StationCode(stationCode1)
          && p.itinerary.get.end == StationCode(stationCode3)
      )
      if selectedPassenger.isDefined then
        sim = s1

    val N_STEPS = 20

    (1 to N_STEPS + 1).foreach(n =>
      val res = sim.doStep()
      res.isRight should be(true)
      val (s, l) = res.toOption.get
      sim = s
    )

    // Assert that the selected passenger arrived at its destination
    sim.state.passengerStates
      .find((pCode, _) =>
        selectedPassenger.get.code == pCode
      ).get._2.currentPosition shouldBe PassengerPosition.AtStation(selectedPassenger.get.itinerary.get.end)
  }
