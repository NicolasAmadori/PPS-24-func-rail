package controller

import model.railway.Railway
import model.simulation.Simulation
import view.SimulationConfigView

import scala.compiletime.uninitialized

class SimulationConfigController(model: Simulation):

  private var view: SimulationConfigView = uninitialized

  def getSimulation: Simulation = model
  def getRailway: Railway = model.railway

  def attachView(view: SimulationConfigView): Unit =
    this.view = view
