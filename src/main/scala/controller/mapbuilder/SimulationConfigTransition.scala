package controller.mapbuilder

import controller.ScreenTransition
import controller.simconfig.SimulationConfigController
import model.mapgrid.MapGrid
import model.railway.Railway
import utils.StageManager
import view.simconfig.SimulationConfigView

class SimulationConfigTransition(mapGrid: MapGrid, model: Railway)
    extends ScreenTransition[SimulationConfigController, SimulationConfigView]:

  def build(): (SimulationConfigController, SimulationConfigView) =
    val controller = SimulationConfigController(mapGrid, model)
    val view = SimulationConfigView(controller)
    (controller, view)

  override def afterAttach(controller: SimulationConfigController, view: SimulationConfigView): Unit =
    view.initGraph()
    StageManager.getStage.title = "Simulation Configurator"
