package controller

import model.railway.Domain.StationCode
import model.railway.Railway
import model.simulation.Simulation
import view.simconfig.SimulationConfigView

class SimulationConfigController(model: Simulation) extends BaseController[SimulationConfigView]:

  def getSimulation: Simulation = model
  def getRailway: Railway = model.railway
  def getStationCodes: List[StationCode] = model.railway.stations.map(_.code)

