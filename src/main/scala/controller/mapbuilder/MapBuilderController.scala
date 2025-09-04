package controller.mapbuilder

import controller.BaseController
import model.mapgrid.{CellType, MapGrid, PlacementError}
import model.railway.RailwayPrologChecker
import model.util.RailwayMapper
import utils.ErrorMessage
import view.mapbuilder.{MapBuilderView, MapBuilderViewError}

class MapBuilderController(var model: MapGrid) extends BaseController[MapBuilderView]:

  private var selectedTool: Option[CellType] = None

  private var onModelUpdated: MapGrid => Unit = _ => ()

  def setOnModelUpdated(callback: MapGrid => Unit): Unit =
    onModelUpdated = callback
    onModelUpdated(model)

  def selectTool(tool: CellType): Unit =
    selectedTool = Some(tool)

  private def validation: Either[MapBuilderViewError, Boolean] =
    if selectedTool.isEmpty then
      Left(MapBuilderViewError.NoToolSelected())
    else
      Right(true)

  private def showError(error: ErrorMessage, title: String): Unit =
    getView.showError(title, error)

  /** Updates the budget with the given value
    *
    * @param the
    *   new value
    */
  def setBudget(value: Int): Unit =
    model = model.setBudget(value)
    onModelUpdated(model)

  /** Disables budget constraints */
  def disableBudget(): Unit =
    model = model.disableBudget
    onModelUpdated(model)

  def placeAt(x: Int, y: Int): Unit =
    validation match
      case Left(error) =>
        showError(error, "Validation failed")
      case Right(_) =>
        model.place(x, y, selectedTool.get) match
          case Right(updatedModel) =>
            model = updatedModel
            onModelUpdated(model)
          case Left(error) =>
            showError(error, s"Placement failed")

  def onNext(): Unit =
    val parsedRailway = RailwayMapper.convert(model)
    val isRailwayConnected = RailwayPrologChecker.isRailwayConnected(parsedRailway)

    (isRailwayConnected, model.isWithinBudget) match
      case (true, true) =>
        new SimulationConfigTransition(model, RailwayMapper.convert(model)).transition()
      case (false, _) =>
        showError(MapBuilderViewError.RailwayNotConnected(), "Invalid railway")
      case (true, false) =>
        showError(PlacementError.OutOfBudget(), "Invalid railway")
