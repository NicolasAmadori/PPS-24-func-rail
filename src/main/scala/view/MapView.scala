package view

import controller.MapController
import model.mapgrid.*
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, RadioButton, ToggleButton, ToggleGroup}
import scalafx.scene.layout.{BorderPane, GridPane, VBox}
import utils.ErrorMessage

class MapView(width: Int, height: Int, controller: MapController) extends BorderPane:

  private val gridPane = new GridPane
  private val toolsGroup = new ToggleGroup
  private val alert = Alert(AlertType.Error)
    alert.title = "Error"

  private val cellSize = 15

  private val smallStationTool = new RadioButton("Small station"):
    toggleGroup = toolsGroup

  private val bigStationTool = new RadioButton("Big station"):
    toggleGroup = toolsGroup

  private val metalRailTool = new RadioButton("Metal rail"):
    toggleGroup = toolsGroup

  private val titaniumRailTool = new RadioButton("Titanium rail"):
    toggleGroup = toolsGroup

  private val toolButtons: Seq[ToggleButton] =
    Seq(smallStationTool, bigStationTool, metalRailTool, titaniumRailTool)

  private val toolPanel = new VBox:
    spacing = 10
    margin = Insets(10)
    children = toolButtons

  left = toolPanel
  center = gridPane

  toolsGroup.selectedToggle.onChange { (_, _, newToggle) =>
    if newToggle != null then
      val selected = newToggle.asInstanceOf[javafx.scene.control.ToggleButton]
      val tool = selected.getText match
        case "Metal rail" => MetalRailPiece
        case "Titanium rail" => TitaniumRailPiece
        case "Small station" => SmallStationPiece
        case "Big station" => BigStationPiece
        case _ => EmptyCell
      controller.selectTool(tool)
  }

  private val buttons: Vector[Vector[Button]] = Vector.tabulate(height, width) { (row, col) =>
    val btn = new Button:
      minWidth = cellSize
      minHeight = cellSize
      maxWidth = cellSize
      maxHeight = cellSize
      style = "-fx-background-color: white"
    btn.onAction = _ => controller.placeAt(col, row)
    gridPane.add(btn, col, row)
    btn
  }

  controller.setOnModelUpdated { model =>
    Platform.runLater {
      for
        y <- 0 until height
        x <- 0 until width
      do
        val cell = model.cells(y)(x)
        val btn = buttons(y)(x)
        btn.style = cell match
          case EmptyCell => "-fx-background-color: white"
          case MetalRailPiece => "-fx-background-color: gray"
          case TitaniumRailPiece => "-fx-background-color: blue"
          case BigStationPiece => "-fx-background-color: red"
          case SmallStationPiece => "-fx-background-color: green"
    }
  }

  def showError(error: ErrorMessage, msg: String = ""): Unit =
    Platform.runLater {
      alert.setContentText(s"$msg: ${error.toString}")
      alert.showAndWait()
    }
