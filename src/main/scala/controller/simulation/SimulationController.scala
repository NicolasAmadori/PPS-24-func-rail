package controller.simulation

import controller.BaseController
import model.simulation.Simulation
import view.simulation.SimulationView

class SimulationController(simulation: Simulation) extends BaseController[SimulationView]:

  private var eventListener: String => Unit = _ => ()

  simulation.start()

  def attachEventListener(listener: (String) => Unit): Unit =
    eventListener = listener
    eventListener("Simulation started")
    eventListener("Train placed")
    eventListener("Train placed")
    eventListener("Train placed")
    eventListener("Train placed")
    eventListener("Train placed")
