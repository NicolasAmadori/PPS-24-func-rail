package controller.simconfig

import controller.ScreenTransition
import controller.simulation.SimulationController
import model.simulation.Simulation
import utils.StageManager
import view.simconfig.{GraphView, RailView, StationView}
import view.simulation.SimulationView

class SimulationTransition(simulation: Simulation, graphView: GraphView[StationView, RailView])
    extends ScreenTransition[SimulationController, SimulationView]:

  def build(): (SimulationController, SimulationView) =
    val controller = SimulationController(simulation)
    val view = SimulationView(graphView)
    (controller, view)

  override def afterAttach(controller: SimulationController, view: SimulationView): Unit =
    StageManager.getStage.title = "Simulation"
    controller.startSimulation()