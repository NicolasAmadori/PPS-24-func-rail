package controller

import controller.simconfig.{HighSpeed, SimulationBuilder, TrainConfig}
import model.entities.EntityCodes.StationCode
import model.simulation.SimulationError.{EmptyTrainName, InvalidDeparture, InvalidRoute}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*
import utils.SampleRailway

import utils.SampleRailway.SampleStation.*

class SimulationBuilderTest extends AnyFlatSpec:

  private val DURATION = 100
  private val railway = SampleRailway.railway2

  "SimulationBuilder" should "build a simulation from a configuration" in {
    val trainsConfig = List(
      TrainConfig(
        0,
        "Train1",
        HighSpeed,
        StationCode(StationA),
        List(StationCode(StationB), StationCode(StationC))
      )
    )
    val simulation = SimulationBuilder.build(DURATION, railway, trainsConfig)
    simulation.isRight should be(true)
    simulation match
      case Right(sim) =>
        sim.state.trains should have size 1
        sim.state.trains.head.code should be("Train1")
        sim.state.trains.head.stations.head should be(StationCode(StationA))
      case Left(_) => fail("Simulation should be built successfully")
  }

  it should "not accept trains with an empty name" in {
    val trainsConfig = List(
      TrainConfig(0, "", HighSpeed, StationCode(StationA), List(StationCode("InvalidStation")))
    )
    val simulation = SimulationBuilder.build(DURATION, railway, trainsConfig)
    simulation.isLeft should be(true)
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
    simulation.isLeft should be(true)
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
    simulation.isLeft should be(true)
    simulation match
      case Left(error) =>
        error.head should be(InvalidDeparture("Train1"))
      case Right(_) => fail("Simulation should not be built with no departure station")
  }
