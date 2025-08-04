package controller

import model.mapgrid.{Cell, MapGrid}
import model.simulation.{Simulation, SimulationState}
import view.simconfig.SimulationConfigView
import view.{GraphUtil, MapView, ViewError}

class MapController(model: MapGrid) extends BaseController[MapView]:

  private var currentModel = model
  private var selectedTool: Option[Cell] = None

  private var onModelUpdated: MapGrid => Unit = _ => ()

  private def getTool: Cell =
    selectedTool.getOrElse(
      throw new IllegalStateException("No tool selected. Call selectTool() first.")
    )

  def setOnModelUpdated(callback: MapGrid => Unit): Unit =
    onModelUpdated = callback

  def selectTool(tool: Cell): Unit =
    selectedTool = Some(tool)

  def placeAt(x: Int, y: Int): Unit =
    validation match
      case Left(error) =>
        getView.showError(error, "Validation failed")
      case Right(_) =>
        val placementResult = currentModel.place(x, y, getTool)
        placementResult match
          case Right(updatedModel) =>
            currentModel = updatedModel
            onModelUpdated(currentModel)
          case Left(error) =>
            getView.showError(error, s"Placement failed")

  private def validation: Either[ViewError, Boolean] =
    if selectedTool == null then
      Left(ViewError.NoToolSelected())
    else
      Right(true)

  def onNext(): Unit =
    val parsedRailway = GraphUtil.createRailway()
    val simulation = Simulation(parsedRailway, SimulationState.empty)

    val transition: ScreenTransition[SimulationConfigController, SimulationConfigView] =
      new ScreenTransition[SimulationConfigController, SimulationConfigView]:
        def build(): (SimulationConfigController, SimulationConfigView) =
          val controller = SimulationConfigController(simulation)
          val view = SimulationConfigView()
          (controller, view)

        override def afterAttach(controller: SimulationConfigController, view: SimulationConfigView): Unit =
          view.initGraph()

    transition.transition()
