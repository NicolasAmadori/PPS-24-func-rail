package controller

import controller.simconfig.{HighSpeed, NormalSpeed, SimulationBuilder, TrainConfig}
import model.railway.Domain.{StationCode, TrainCode}
import model.simulation.SimulationError.{EmptyTrainName, InvalidDeparture, InvalidRoute}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*
import view.GraphUtil

class SimulationBuilderTest extends AnyFlatSpec:

  private val railway = GraphUtil.createRailway()

  "SimulationBuilder" should "build a simulation from a configuration" in {
    val trainsConfig = List(
      TrainConfig(0, "Train1", HighSpeed, StationCode(GraphUtil.STATION_A), List(StationCode(GraphUtil.STATION_B), StationCode(GraphUtil.STATION_C))),
    )
    val simulation = SimulationBuilder.build(railway, trainsConfig)
    simulation.isRight should be(true)
    simulation match
      case Right(sim) =>
        sim.state.trains should have size 1
        sim.state.trains.head.code should be("Train1")
        sim.state.trains.head.stations.head should be(StationCode(GraphUtil.STATION_A))
      case Left(_) => fail("Simulation should be built successfully")

  }

  it should "not accept trains with an empty name" in {
    val trainsConfig = List(
      TrainConfig(0, "", HighSpeed, StationCode(GraphUtil.STATION_A), List(StationCode("InvalidStation")))
    )
    val simulation = SimulationBuilder.build(railway, trainsConfig)
    simulation.isLeft should be(true)
    simulation match
      case Left(error) =>
        error.head should be(EmptyTrainName())
      case Right(_) => fail("Simulation should not be built with invalid stations")
  }

  it should "not accept trains with invalid stations" in {
    val trainsConfig = List(
      TrainConfig(0, "Train1", HighSpeed, StationCode(GraphUtil.STATION_A), List(StationCode("InvalidStation")))
    )
    val simulation = SimulationBuilder.build(railway, trainsConfig)
    simulation.isLeft should be(true)
    simulation match
      case Left(error) =>
        error.head should be(InvalidRoute(TrainCode("Train1")))
      case Right(_) => fail("Simulation should not be built with invalid stations")
  }
  
  it should "not accept trains with no departure station" in {
    val trainsConfig = List(
      TrainConfig(0, "Train1", HighSpeed, StationCode.empty, List(StationCode(GraphUtil.STATION_B)))
    )
    val simulation = SimulationBuilder.build(railway, trainsConfig)
    simulation.isLeft should be(true)
    simulation match
      case Left(error) =>
        error.head should be(InvalidDeparture(TrainCode("Train1")))
      case Right(_) => fail("Simulation should not be built with no departure station")
  }
