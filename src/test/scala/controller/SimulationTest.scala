package controller

import model.entities.EntityCodes.{StationCode, TrainCode}
import model.entities.Train
import model.simulation.{RouteHelper, Simulation}
import org.scalatest.flatspec.AnyFlatSpec
import util.SampleRailway
import model.entities.Train.normalTrain
import model.simulation.TrainPosition.{AtStation, OnRail}
import org.scalatest.matchers.should.Matchers.*
import util.SampleRailway.SampleStation.{StationB, StationE}

class SimulationTest extends AnyFlatSpec:

  private val trainCode1 = "T1"
  private val trainCode2 = "T2"

  private val railway = SampleRailway.railway3
  private val train1 = normalTrain(trainCode1, List(StationB, StationE).map(StationCode(_)))
  private val train2 = normalTrain(trainCode2, List(StationB, StationE).map(StationCode(_)))
  private val route = RouteHelper.getRouteForTrain(train1, railway).get
  private val trainWithRoute1 = train1.withRoute(route)
  private val trainWithRoute2 = train2.withRoute(route)

  private def createSimulationWithTrains(trains: List[Train]): Simulation =
    Simulation.withRailway(1, railway).addTrains(trains).start()

  extension (simulation: Simulation)
    def loopFor(steps: Int): Simulation =
      val loopedSimulation = Range(0, steps).foldLeft(simulation) { (acc, _) =>
        acc.doStep() match
          case Right(sim) => sim._1
          case Left(e) => fail(e.toString)
      }
      loopedSimulation

  "A train state" should "be updated in a simulation step" in {
    val simulation = createSimulationWithTrains(List(trainWithRoute1))
    val updatedSim = simulation.loopFor(1)

    val state = updatedSim.state
    state.simulationStep should be(1)
    val rail = route.getRailAt(0)
    state.railStates(rail.code).free should be(false)
    val trainState = state.trainStates(TrainCode(trainCode1))
    trainState.position should be(OnRail(rail.code))
    trainState.travelTime should be(train1.getTravelTime(rail))
  }

  it should "stay on the rail until crossed" in {
    val travelTime = train1.getTravelTime(route.getRailAt(0))
    val simulation = createSimulationWithTrains(List(trainWithRoute1))
    val loopedSimulation = simulation.loopFor(travelTime)

    val state = loopedSimulation.state
    val trainState = state.trainStates(TrainCode(trainCode1))
    val rail = route.getRailAt(0)
    state.simulationStep should be(10)
    state.railStates(rail.code).free should be(false)
    trainState.position should be(OnRail(rail.code))
  }

  it should "enter station when rail crossed" in {
    val travelTime = train1.getTravelTime(route.getRailAt(0))
    val simulation = createSimulationWithTrains(List(trainWithRoute1))
    val loopedSimulation = simulation.loopFor(travelTime + 1)

    val state = loopedSimulation.state
    val trainState = state.trainStates(TrainCode(trainCode1))
    val rail = route.getRailAt(0)
    state.simulationStep should be(travelTime + 1)
    state.railStates(rail.code).free should be(true)
    trainState.position should be(AtStation(rail.stationB))
  }

  it should "not allow two trains to be on the same rail" in {
    val simulation = createSimulationWithTrains(List(trainWithRoute1, trainWithRoute2))
    val loopedSimulation = simulation.loopFor(1)

    val state = loopedSimulation.state
    val trainState1 = state.trainStates(TrainCode(trainCode1))
    val trainState2 = state.trainStates(TrainCode(trainCode2))
    val rail = route.getRailAt(0)
    trainState1.position should be(OnRail(rail.code))
    trainState2.position should be(AtStation(rail.stationA))
    state.railStates(rail.code).free should be(false)
    trainState1.position should not equal (trainState2.position)
  }
