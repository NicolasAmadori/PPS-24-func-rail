package controller.mapbuilder

import controller.BaseController
import model.mapgrid.{CellType, MapGrid, PlacementError}
import model.railway.RailwayPrologChecker
import model.util.RailwayMapper
import utils.ErrorMessage
import view.mapbuilder.{MapBuilderView, MapBuilderViewError}

/** The controller for the map builder. It allows to build a map grid by selecting tools and placing items on the grid
  * enforcing validation rules.
  */
class MapBuilderController(var model: MapGrid) extends BaseController[MapBuilderView]:

  private var selectedTool: Option[CellType] = None

  private var onModelUpdated: MapGrid => Unit = _ => ()

  /** Sets the callback to execute when there is an update in the model.
    * @param callback
    */
  def setOnModelUpdated(callback: MapGrid => Unit): Unit =
    onModelUpdated = callback
    onModelUpdated(model)

  /** Sets the current selected tool to the given type.
    * @param tool
    *   the [[model.mapgrid.CellType]] selected
    */
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

  /** Fills the given position with the current selected tool, shows an error if no tool is selected.
    * @param x
    * @param y
    */
  def placeAt(x: Int, y: Int): Unit =
    validation match
      case Left(error) =>
        showError(error, "Validation failed")
      case Right(_) =>
        val placementResult = model.place(x, y, selectedTool.get)
        placementResult match
          case Right(updatedModel) =>
            model = updatedModel
            onModelUpdated(model)
          case Left(error) =>
            showError(error, s"Placement failed")

  /** Parses the map grid and performs validity checks. If the railway is valid, it transitions to the next step. Shows
    * an error otherwise.
    */
  def onNext(): Unit =
    val parsedRailway = RailwayMapper.convert(model)
    val isRailwayConnected = RailwayPrologChecker.isRailwayConnected(parsedRailway)
    if isRailwayConnected && model.isWithinBudget then
      val transition = new SimulationConfigTransition(model, parsedRailway)
      transition.transition()
    else if !isRailwayConnected then
      showError(
        MapBuilderViewError.RailwayNotConnected(),
        s"Invalid railway"
      )
    else
      showError(
        PlacementError.OutOfBudget(),
        s"Invalid railway"
      )
