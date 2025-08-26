package model.simulation

import model.entities.EntityCodes.{StationCode, TrainCode}
import model.entities.Train
import model.entities.dsl.buildNormalTrain
import model.simulation.TrainPosition.{AtStation, OnRail}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*
import util.SampleRailway
import util.SampleRailway.SampleStation.{StationA, StationB}
import util.loopFor

class TrainStateProgressTest extends AnyFlatSpec:

  private val setupLoop = 1
  private val enterStationLoop = 1
  private val trainCode1 = "T1"
  private val trainCode2 = "T2"

  private val railway = SampleRailway.railway3
  private val train1 = buildNormalTrain(trainCode1):
    _ departsFrom StationCode(StationA) stopsAt StationCode(StationB)
  private val train2 = buildNormalTrain(trainCode2):
    _ departsFrom StationCode(StationA) stopsAt StationCode(StationB)
  private val route = RouteHelper.getRouteForTrain(train1, railway).get
  private val trainWithRoute1 = train1.withRoute(route)
  private val trainWithRoute2 = train2.withRoute(route)

  private def createSimulationWithTrains(trains: List[Train]): Simulation =
    val (simulation, logs) = Simulation.withRailway(1, railway).addTrains(trains).start()
    simulation

  "Train state" should "be updated in a simulation step" in {
    val simulation = createSimulationWithTrains(List(trainWithRoute1))
    val updatedSim = simulation.loopFor(setupLoop + 1)

    val state = updatedSim.state
    state.simulationStep should be(setupLoop + 1)
    val rail = route.getRailAt(0)
    state.railStates(rail.code).isFree should be(false)
    val trainState = state.trainStates(TrainCode(trainCode1))
    trainState.position.get should be(OnRail(rail.code))
    trainState.travelTime should be(train1.getTravelTime(rail))
  }

  it should "stay on the rail until crossed" in {
    val travelTime = train1.getTravelTime(route.getRailAt(0))
    val simulation = createSimulationWithTrains(List(trainWithRoute1))
    val loopedSimulation = simulation.loopFor(travelTime + setupLoop)

    val state = loopedSimulation.state
    val trainState = state.trainStates(TrainCode(trainCode1))
    val rail = route.getRailAt(0)
    state.simulationStep should be(travelTime + setupLoop)
    state.railStates(rail.code).isFree should be(false)
    trainState.position.get should be(OnRail(rail.code))
  }

  it should "enter station when rail crossed" in {
    val travelTime = train1.getTravelTime(route.getRailAt(0))
    val simulation = createSimulationWithTrains(List(trainWithRoute1))
    val loopedSimulation = simulation.loopFor(setupLoop + travelTime + enterStationLoop)

    val state = loopedSimulation.state
    val trainState = state.trainStates(TrainCode(trainCode1))
    val rail = route.getRailAt(0)
    state.simulationStep should be(setupLoop + travelTime + enterStationLoop)
    state.railStates(rail.code).isFree should be(true)
    trainState.position.get should be(AtStation(rail.stationB))
  }

  it should "not allow two trains to be on the same rail" in {
    val simulation = createSimulationWithTrains(List(trainWithRoute1, trainWithRoute2))
    val loopedSimulation = simulation.loopFor(setupLoop + 1)

    val state = loopedSimulation.state
    val trainState1 = state.trainStates(TrainCode(trainCode1))
    val trainState2 = state.trainStates(TrainCode(trainCode2))
    val rail = route.getRailAt(0)
    trainState1.position.get should be(OnRail(rail.code))
    trainState2.position.get should be(AtStation(rail.stationA))
    state.railStates(rail.code).isFree should be(false)
    trainState1.position.get should not equal (trainState2.position)
  }

  it should "correctly invert direction of trains when end of line reached" in {
    val simulation = createSimulationWithTrains(List(trainWithRoute1))
    val travelTime = trainWithRoute1.getTravelTime(route.getRailAt(0))
    val loopedSimulation = simulation.loopFor(setupLoop + travelTime + enterStationLoop + 1)

    val state = loopedSimulation.state
    val trainState = state.trainStates(TrainCode(trainCode1))
    val rail = route.getRailAt(0)
    trainState.position.get should be(OnRail(rail.code))
    state.railStates(rail.code).isFree should be(false)
    trainState.forward should be(false)
  }
