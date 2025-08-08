package controller.mapbuilder

import controller.simconfig.SimulationConfigController
import controller.{BaseController, ScreenTransition}
import model.mapgrid.{CellType, MapGrid}
import model.railway.Railway
import model.util.RailwayMapper
import utils.{ErrorMessage, StageManager}
import view.mapbuilder.{MapBuilderView, MapBuilderViewError}
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

class MapBuilderController(model: MapGrid) extends BaseController[MapBuilderView]:

  private var currentModel = model
  private var selectedTool: Option[CellType] = None

  private var onModelUpdated: MapGrid => Unit = _ => ()

  def setOnModelUpdated(callback: MapGrid => Unit): Unit =
    onModelUpdated = callback
    onModelUpdated(currentModel)

  def selectTool(tool: CellType): Unit =
    selectedTool = Some(tool)

  private def validation: Either[MapBuilderViewError, Boolean] =
    if selectedTool.isEmpty then
      Left(MapBuilderViewError.NoToolSelected())
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

  def onNext(): Unit =
    val parsedRailway = RailwayMapper.convert(currentModel)
    val transition = new SimulationConfigTransition(currentModel, parsedRailway)
    transition.transition()
