package controller

import model.railway.Railway
import model.simulation.Simulation
import view.simconfig.SimulationConfigView

class SimulationConfigController(model: Simulation):

  private var view: Option[SimulationConfigView] = None

  def getSimulation: Simulation = model
  def getRailway: Railway = model.railway

  def attachView(view: SimulationConfigView): Unit =
    this.view = Some(view)
