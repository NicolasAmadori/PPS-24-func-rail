package controller.simconfig

import controller.BaseController
import model.railway.Domain.StationCode
import model.railway.Railway
import model.simulation.Simulation
import view.simconfig.SimulationConfigView

/** Controller for managing the simulation configuration view.
  * @param model
  *   the Railway model that contains the railway data
  */
class SimulationConfigController(model: Railway) extends BaseController[SimulationConfigView]:

  private var localState: SimulationFormState = SimulationFormState()

  /** Returns the Railway model associated with this controller.
    * @return
    *   the Railway model
    */
  def getRailway: Railway = model

  /** Returns the list of StationCode associated with this controller.
    * @return
    *   the List of codes of the stations in the railway model
    */
  def getStationCodes: List[StationCode] = model.stations.map(_.code)

  /** Returns the id of the newly added empty train.
    * @return
    *   the id of the train
    */
  def addTrain(): Int =
    val (id, newState) = localState.addTrain()
    localState = newState
    id

  /** Remove a train by the given id.
    *
    * @param trainId
    *   The id of the train to remove
    */
  def removeTrain(trainId: Int): Unit =
    localState = localState.removeTrain(trainId)

  /** Updates the name of a train in the local state.
    * @param id
    *   the id of the train to update
    * @param name
    *   the new name for the train
    */
  def updateTrainName(id: Int, name: String): Unit =
    localState = localState.updateTrainName(id, name)

  /** Sets the train type to high speed for a given train id.
    * @param id
    *   the id of the train to update
    */
  def setHighSpeedTrain(id: Int): Unit =
    localState = localState.setHighSpeedTrain(id)

  /** Sets the train type to normal speed for a given train id.
    * @param id
    *   the id of the train to update
    */
  def setNormalSpeedTrain(id: Int): Unit =
    localState = localState.setNormalSpeedTrain(id)

  /** Sets the departure station for a given train id.
    * @param id
    *   the id of the train to update
    * @param station
    *   the StationCode of the departure station
    */
  def setDepartureStation(id: Int, station: StationCode): Unit =
    localState = localState.setDepartureStation(id, station)

  /** Adds a stop to a train in the local state.
    * @param id
    *   the id of the train to update
    * @param station
    *   the StationCode of the stop to add
    */
  def addStop(id: Int, station: StationCode): Unit =
    localState = localState.addStop(id, station)

  /** Removes a stop from a train in the local state.
    * @param id
    *   the id of the train to update
    * @param station
    *   the StationCode of the stop to remove
    */
  def removeStop(id: Int, station: StationCode): Unit =
    localState = localState.removeStop(id, station)

  /** Start the simulation by building the Simulation object and transitioning to the simulation view */
  def startSimulation(): Unit =
    val simulation = SimulationBuilder.build(model, localState.trains)
    simulation match
      case Left(error) => getView.showErrors(error)
      case Right(sim) =>
        println("Transitioning to simulation view")
