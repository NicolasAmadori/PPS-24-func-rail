package model.simulation

import model.entities.EntityCodes.StationCode
import model.entities.{Rail, Station, Train}
import model.entities.Rail.metalRail
import model.railway.Railway
import model.entities.Train.{highSpeedTrain, normalTrain}
import model.util.PassengerGenerator
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class SimulationTest extends AnyFlatSpec:

  val stationCode1: String = "ST001"
  val stationCode2: String = "ST002"
  val stationCode3: String = "ST003"
  val rail1: String = "MR1"
  val rail2: String = "MR2"
  val trainCode1: String = "101"
  val trainCode2: String = "202"

  val simulationDuration = 100

  val train1: Train = normalTrain(trainCode1, StationCode.listOf(stationCode1, stationCode2))
  val train2: Train = highSpeedTrain(trainCode2, StationCode.listOf(stationCode2, stationCode3))

  val railway: Railway = Railway.withStations(
    List(Station.smallStation(stationCode1), Station.bigStation(stationCode2), Station.smallStation(stationCode3))
  ).withRails(
    List(metalRail(rail1, 200, stationCode1, stationCode2), metalRail(rail2, 500, stationCode2, stationCode3))
  )

  "A SimulationState" should "be empty initially" in {
    val state = SimulationState.empty

    state.trains should be(empty)
  }

  it can "be created with trains" in {
    val state = SimulationState(List(train1, train2))

    state.trains should contain theSameElementsAs List(train1, train2)
  }

  it should "allow adding a train" in {
    val state = SimulationState.empty.withTrains(List(train1))

    state.trains should contain(train1)
  }

  it should "not allow adding a train with the same code" in {
    val train = normalTrain(trainCode1, StationCode.listOf(stationCode3, stationCode2))
    val state = SimulationState(List(train)).withTrains(List(train))

    state.trains.count(_.code == train.code) should be(1)
  }

  "A Simulation" should "be created with a Railway and a SimulationState with trains" in {
    val state = SimulationState(List(train1, train2))
    val simulation = Simulation(simulationDuration, railway, state, PassengerGenerator(railway, state.trains))

    simulation.railway should be(railway)
  }

  it should "not allow adding a train with an invalid route" in {
    val invalidTrain = normalTrain(trainCode1, StationCode.listOf(stationCode1, "INVALID_STATION"))
    val simulation = Simulation(
      simulationDuration,
      railway,
      SimulationState.empty,
      PassengerGenerator(railway, SimulationState.empty.trains)
    )
    assertThrows[IllegalArgumentException](simulation.withTrains(List(invalidTrain)))
  }
