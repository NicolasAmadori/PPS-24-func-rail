package controller.simconfig

import controller.{BaseController, MapController, ScreenTransition}
import model.mapgrid.MapGrid
import model.railway.Domain.StationCode
import model.railway.Railway
import model.simulation.Simulation
import utils.StageManager
import view.MapView
import view.simconfig.SimulationConfigView

class MapBuilderTransition(model: MapGrid)
    extends ScreenTransition[MapController, MapView]:

  def build(): (MapController, MapView) =
    val controller = MapController(model)
    val view = MapView(model.width, model.height, controller)
    (controller, view)

  override def afterAttach(controller: MapController, view: MapView): Unit =
    StageManager.getStage.title = "Map Builder"

/** Controller for managing the simulation configuration view.
  * @param model
  *   the Railway model that contains the railway data
  */
class SimulationConfigController(mapGrid: MapGrid, model: Railway)
    extends BaseController[SimulationConfigView]:

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

  def onBack(): Unit =
    val transition = new MapBuilderTransition(mapGrid)
    transition.transition()
