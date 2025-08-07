package controller.simconfig

import controller.BaseController
import model.railway.Domain.StationCode
import model.railway.Railway
import model.simulation.Simulation
import view.simconfig.SimulationConfigView

class SimulationConfigController(model: Railway) extends BaseController[SimulationConfigView]:

  private var localState: SimulationFormState = SimulationFormState()

  def getRailway: Railway = model
  def getStationCodes: List[StationCode] = model.stations.map(_.code)

  def addTrain(): Int =
    val (id, newState) = localState.addTrain()
    localState = newState
    id

  def updateTrainName(id: Int, name: String): Unit =
    localState = localState.updateTrainName(id, name)

  def setHighSpeedTrain(id: Int): Unit =
    localState = localState.setHighSpeedTrain(id)

  def setNormalSpeedTrain(id: Int): Unit =
    localState = localState.setNormalSpeedTrain(id)

  def setDepartureStation(id: Int, station: StationCode): Unit =
    localState = localState.setDepartureStation(id, station)

  def addStop(id: Int, station: StationCode): Unit =
    localState = localState.addStop(id, station)

  def removeStop(id: Int, station: StationCode): Unit =
    localState = localState.removeStop(id, station)

  def startSimulation(): Unit =
    val simulation = SimulationBuilder.build(model, localState.trains)
    simulation match
      case Left(error) => getView.showErrors(error)
      case Right(sim) =>
        println("Transitioning to simulation view")
