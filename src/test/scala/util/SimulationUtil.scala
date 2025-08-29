package util

import model.simulation.Simulation
import org.scalatest.Assertions.fail

extension (simulation: Simulation)
  def loopFor(steps: Int): Simulation =
    val loopedSimulation = Range(0, steps).foldLeft(simulation) { (acc, _) =>
      acc.doStep() match
        case Right(sim) => sim._1
        case Left(e) => fail(e.toString)
    }
    loopedSimulation
