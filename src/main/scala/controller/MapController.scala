package controller

import model.mapgrid.{Cell, MapGrid}
import view.{MapView, ViewError}

import scala.compiletime.uninitialized

class MapController(model: MapGrid):

  private var currentModel = model
  private var selectedTool: Cell = uninitialized
  private var view: MapView = uninitialized

  private var onModelUpdated: MapGrid => Unit = _ => ()

  def attachView(view: MapView): Unit =
    this.view = view

  def setOnModelUpdated(callback: MapGrid => Unit): Unit =
    onModelUpdated = callback

  def selectTool(tool: Cell): Unit =
    selectedTool = tool

  def placeAt(x: Int, y: Int): Unit =
    validation match
      case Left(error) =>
        view.showError(error, "Validation failed")
      case Right(_) =>
        val placementResult = currentModel.place(x, y, selectedTool)
        placementResult match
          case Right(updatedModel) =>
            currentModel = updatedModel
            onModelUpdated(currentModel)
          case Left(error) =>
            view.showError(error, s"Placement failed")

  private def validation: Either[ViewError, Boolean] =
    if view == null then
      Left(ViewError.NotAttached())
    else if selectedTool == null then
      Left(ViewError.NoToolSelected())
    else
      Right(true)
