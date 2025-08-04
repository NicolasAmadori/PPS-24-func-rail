package controller

import model.mapgrid.{Cell, MapGrid}
import model.simulation.{Simulation, SimulationState}

import utils.StageManager
import view.simconfig.SimulationConfigView
import view.{GraphUtil, MapView, ViewError}

class MapController(model: MapGrid):

  private var currentModel = model
  private var selectedTool: Option[Cell] = None
  private var view: Option[MapView] = None

  private var onModelUpdated: MapGrid => Unit = _ => ()

  def attachView(view: MapView): Unit =
    this.view = Some(view)

  def getView: MapView = view.getOrElse(
    throw new IllegalStateException("View has not been attached. Call attachView() first.")
  )

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
    if view == null then
      Left(ViewError.NotAttached())
    else if selectedTool == null then
      Left(ViewError.NoToolSelected())
    else
      Right(true)

  def parseMap(): Unit =
    val parsedRailway = GraphUtil.createRailway()
    val simulation = new Simulation(parsedRailway, SimulationState.empty)
    val newController = new SimulationConfigController(simulation)
    val newView = new SimulationConfigView()
    newController.attachView(newView)
    StageManager.setRoot(newView.getRoot)
    newView.initGraph()
