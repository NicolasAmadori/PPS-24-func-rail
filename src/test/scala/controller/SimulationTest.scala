package controller

import model.entities.EntityCodes.{StationCode, TrainCode}
import model.simulation.{RouteHelper, Simulation, SimulationState}
import org.scalatest.flatspec.AnyFlatSpec
import util.SampleRailway
import model.entities.Train.normalTrain
import model.simulation.TrainPosition.OnRail
import org.scalatest.matchers.should.Matchers.*
import util.SampleRailway.SampleStation.{StationB, StationE}

class SimulationTest extends AnyFlatSpec:

  private val railway = SampleRailway.railway3
  private val train = normalTrain("T1", List(StationB, StationE).map(StationCode(_)))
  private val route = RouteHelper.getRouteForTrain(train, railway).get
  private val trainWithRoute = train.withRoute(route)

  "A train state" should "be updated in a simulation step" in {
    val simulation = Simulation.withRailway(1, railway).addTrains(List(trainWithRoute)).start()
    val updatedSim = simulation.doStep()

    updatedSim match
      case Right((s, _)) =>
        val state = s.state
        state.simulationStep should be(1)
        val rail = route.getRailAt(0)
        state.railStates(rail.code).free should be(false)
        val trainState = state.trainStates(TrainCode("T1"))
        trainState.position should be(OnRail(rail.code))
        trainState.travelTime should be(math.ceil(rail.length / train.speed))
      case Left(e) => fail(e.toString)
  }


