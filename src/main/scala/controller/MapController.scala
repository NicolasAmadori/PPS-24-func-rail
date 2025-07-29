package controller

import model.{Cell, EmptyCell, MapGrid, MetalRailPiece}

class MapController(model: MapGrid):

  private var currentModel = model
  private var selectedTool: Cell = MetalRailPiece

  private var onModelUpdated: MapGrid => Unit = _ => ()

  def setOnModelUpdated(callback: MapGrid => Unit): Unit =
    onModelUpdated = callback

  def selectTool(tool: Cell): Unit =
    selectedTool = tool

  def placeAt(x: Int, y: Int): Unit =
    val placementResult = currentModel.place(x, y, selectedTool)
    placementResult match
      case Right(updatedModel) =>
        currentModel = updatedModel
        onModelUpdated(currentModel)
      case Left(error) =>
        println(s"Placement error: $error") // TODO: notificare l'utente in modo più elegante
