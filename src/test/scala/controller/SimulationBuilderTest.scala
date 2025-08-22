package controller

import controller.simconfig.{HighSpeed, NormalSpeed, SimulationBuilder, TrainConfig}
import model.entities.EntityCodes.{StationCode, TrainCode}
import model.simulation.SimulationError.{CannotComputeRoute, EmptyTrainName, InvalidDeparture, InvalidRoute}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*
import util.SampleRailway
import SampleRailway.SampleStation.*

class SimulationBuilderTest extends AnyFlatSpec:

  private val DURATION = 100
  private val railway = SampleRailway.railway2

  "SimulationBuilder" should "build a simulation from a configuration both for a normal and a high speed train" in {
    val trainsConfig = List(
      TrainConfig(
        0,
        "Train1",
        HighSpeed,
        StationCode(StationA),
        List(StationCode(StationB), StationCode(StationC))
      ),
      TrainConfig(
        1,
        "Train2",
        NormalSpeed,
        StationCode(StationB),
        List(StationCode(StationA))
      )
    )
    val simulation = SimulationBuilder.build(DURATION, railway, trainsConfig)
    simulation match
      case Right(sim) =>
        sim.state.trains should have size 2
        sim.state.trains.head.code should be("Train1")
        sim.state.trains.head.departureStation should be(StationCode(StationA))
      case Left(e) => fail(s"Simulation retrieved an error $e")
  }

  it should "not accept trains with an empty name" in {
    val trainsConfig = List(
      TrainConfig(0, "", HighSpeed, StationCode(StationA), List(StationCode("InvalidStation")))
    )
    val simulation = SimulationBuilder.build(DURATION, railway, trainsConfig)
    simulation match
      case Left(error) =>
        error.head should be(EmptyTrainName())
      case Right(_) => fail("Simulation should not be built with invalid stations")
  }

  it should "not accept trains with invalid stations" in {
    val trainsConfig = List(
      TrainConfig(0, "Train1", HighSpeed, StationCode(StationA), List(StationCode("InvalidStation")))
    )
    val simulation = SimulationBuilder.build(DURATION, railway, trainsConfig)
    simulation match
      case Left(error) =>
        error.head should be(InvalidRoute("Train1"))
      case Right(_) => fail("Simulation should not be built with invalid stations")
  }

  it should "not accept trains with no departure station" in {
    val trainsConfig = List(
      TrainConfig(0, "Train1", HighSpeed, StationCode.empty, List(StationCode(StationB)))
    )
    val simulation = SimulationBuilder.build(DURATION, railway, trainsConfig)
    simulation match
      case Left(error) =>
        error.head should be(InvalidDeparture("Train1"))
      case Right(_) => fail("Simulation should not be built with no departure station")
  }

  it should "not succeed if a train has no route" in {
    val trainsConfig = List(
      TrainConfig(0, "Train1", NormalSpeed, StationCode(StationA), List(StationCode(StationC)))
    )
    val simulation = SimulationBuilder.build(DURATION, railway, trainsConfig)
    simulation match
      case Left(error) =>
        error.head should be(CannotComputeRoute(TrainCode("Train1")))
      case Right(_) => fail("Simulation should not be built with no route for a train")
  }
