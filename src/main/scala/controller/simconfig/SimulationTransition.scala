package controller.simconfig

import controller.ScreenTransition
import controller.simulation.SimulationController
import model.simulation.Simulation
import utils.StageManager
import view.simulation.SimulationView

class SimulationTransition(simulation: Simulation) extends ScreenTransition[SimulationController, SimulationView]:

  def build(): (SimulationController, SimulationView) =
    val controller = SimulationController(simulation)
    val view = SimulationView(controller)
    (controller, view)

  override def afterAttach(controller: SimulationController, view: SimulationView): Unit =
    StageManager.getStage.title = "Simulation"
