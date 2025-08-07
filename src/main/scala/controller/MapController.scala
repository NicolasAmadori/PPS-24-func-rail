package controller

import controller.simconfig.SimulationConfigController
import model.mapgrid.{CellType, MapGrid}
import model.simulation.{Simulation, SimulationState}
import model.util.RailwayMapper
import model.railway.Railway

import utils.{ErrorMessage, StageManager}
import view.simconfig.SimulationConfigView
import view.{MapView, ViewError}

class SimulationConfigTransition(width: Int, height: Int, mapGrid: MapGrid, model: Railway)
    extends ScreenTransition[SimulationConfigController, SimulationConfigView]:

  def build(): (SimulationConfigController, SimulationConfigView) =
    val controller = SimulationConfigController(width, height, mapGrid, model)
    val view = SimulationConfigView(controller)
    (controller, view)

  override def afterAttach(controller: SimulationConfigController, view: SimulationConfigView): Unit =
    view.initGraph()
    StageManager.getStage.title = "Simulation Configurator"

class MapController(model: MapGrid) extends BaseController[MapView]:

  private var currentModel = model
  private var selectedTool: Option[CellType] = None

  private var onModelUpdated: MapGrid => Unit = _ => ()

  def setOnModelUpdated(callback: MapGrid => Unit): Unit =
    onModelUpdated = callback
    onModelUpdated(currentModel)

  def selectTool(tool: CellType): Unit =
    selectedTool = Some(tool)

  private def validation: Either[ViewError, Boolean] =
    if selectedTool.isEmpty then
      Left(ViewError.NoToolSelected())
    else
      Right(true)

  private def showError(error: ErrorMessage, title: String): Unit =
    getView.showError(error, title)

  def placeAt(x: Int, y: Int): Unit =
    validation match
      case Left(error) =>
        showError(error, "Validation failed")
      case Right(_) =>
        val placementResult = currentModel.place(x, y, selectedTool.get)
        placementResult match
          case Right(updatedModel) =>
            currentModel = updatedModel
            onModelUpdated(currentModel)
          case Left(error) =>
            showError(error, s"Placement failed")

  def onNext(width: Int, height: Int): Unit =
    val parsedRailway = RailwayMapper.convert(currentModel)
    Simulation(parsedRailway, SimulationState.empty)

    val transition = new SimulationConfigTransition(width, height, currentModel, parsedRailway)
    transition.transition()
