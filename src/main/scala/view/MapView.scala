package view

import controller.MapController
import model.mapgrid.*
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.Parent
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, RadioButton, ToggleGroup}
import scalafx.scene.layout.{BorderPane, GridPane, VBox}
import utils.ErrorMessage

object ToolMappings:
  val nameToCell: Map[String, Cell] = Map(
    "Metal rail" -> MetalRailPiece,
    "Titanium rail" -> TitaniumRailPiece,
    "Small station" -> SmallStationPiece,
    "Big station" -> BigStationPiece
  )

  val cellToColor: Map[Cell, String] = Map(
    EmptyCell -> "white",
    MetalRailPiece -> "gray",
    TitaniumRailPiece -> "blue",
    BigStationPiece -> "red",
    SmallStationPiece -> "green"
  )

object MapViewConstants:
  val DefaultColor = "white"
  val DefaultCellSize = 15

class MapView(width: Int, height: Int, controller: MapController) extends BorderPane, View:

  import ToolMappings.*
  import MapViewConstants.*

  private val gridPane = new GridPane
  private val toolsGroup = new ToggleGroup
  private val toolButtons = createToolButtons()
  private val buttons = createGrid(DefaultCellSize)

  private val alert = new Alert(AlertType.Error):
    title = "Error"

  private val toolPanel = new VBox:
    spacing = 10
    margin = Insets(10)
    children = toolButtons ++ Seq(
      new Button("Parse map"):
        onAction = _ => controller.onNext()
    )

  left = toolPanel
  center = gridPane
  initialize()

  private def initialize(): Unit =
    setupModelListener()
    setupToolListener()

  private def createGrid(cellSize: Int): Vector[Vector[Button]] =
    Vector.tabulate(height, width) { (row, col) =>
      val btn = new Button:
        minWidth = cellSize
        minHeight = cellSize
        maxWidth = cellSize
        maxHeight = cellSize
        style = toCssColor(DefaultColor)
      btn.onAction = _ => controller.placeAt(col, row)
      gridPane.add(btn, col, row)
      btn
    }

  private def createToolButtons(): Seq[RadioButton] =
    nameToCell.keys.toSeq.map: label =>
      new RadioButton(label):
        toggleGroup = toolsGroup

  private def setupToolListener(): Unit =
    toolsGroup.selectedToggle.onChange { (_, _, newToggle) =>
      Option(newToggle).collect {
        case rb: RadioButton => nameToCell.getOrElse(rb.getText, EmptyCell)
      }.foreach(controller.selectTool)
    }

  private def setupModelListener(): Unit =
    controller.setOnModelUpdated { model =>
      Platform.runLater:
        for y <- 0 until height; x <- 0 until width do
          val cell = model.cells(y)(x)
          buttons(y)(x).style = toCssColor(cellToColor.getOrElse(cell, DefaultColor))
    }

  private def toCssColor(color: String): String = s"-fx-background-color: $color"

  def showError(error: ErrorMessage, msg: String = ""): Unit =
    Platform.runLater:
      alert.setContentText(s"$msg: $error")
      alert.showAndWait()

  override def getRoot: Parent = this
