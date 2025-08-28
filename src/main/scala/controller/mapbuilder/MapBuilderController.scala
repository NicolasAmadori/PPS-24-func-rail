package controller.mapbuilder

import controller.BaseController
import model.mapgrid.{CellType, MapGrid}
import model.railway.RailwayPrologChecker
import model.util.RailwayMapper
import utils.ErrorMessage
import view.mapbuilder.{MapBuilderView, MapBuilderViewError}

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
    getView.showError(title, error)

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
    val isRailwayConnected = RailwayPrologChecker.isRailwayConnected(parsedRailway)
    if isRailwayConnected then
      val transition = new SimulationConfigTransition(currentModel, parsedRailway)
      transition.transition()
    else
      showError(
        MapBuilderViewError.RailwayNotConnected(),
        s"Invalid railway"
      ) // The railway is invalid beacuse it is not connected.
