package view

import controller.MapController
import model.mapgrid.*
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.{Node, Parent}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, RadioButton, ToggleGroup}
import scalafx.scene.layout.{BorderPane, GridPane, VBox}
import utils.ErrorMessage
import view.simconfig.SimulationConfigViewConstants.{DefaultPadding, DefaultSpacing}

object ToolMappings:
  val railNameToCell: Map[String, CellType] = Map(
    "Metal rail" -> MetalRailType,
    "Titanium rail" -> TitaniumRailType
  )

  val stationNameToCell: Map[String, CellType] = Map(
    "Small station" -> SmallStationType,
    "Big station" -> BigStationType
  )

  val cellToColor: Map[CellType, String] = Map(
    EmptyType -> "white",
    MetalRailType -> "gray",
    TitaniumRailType -> "blue",
    BigStationType -> "red",
    SmallStationType -> "green"
  )

object MapViewConstants:
  val DefaultColor = "white"
  val DefaultCellSize = 15

class MapView(width: Int, height: Int, controller: MapController) extends BorderPane, View:

  import ToolMappings.*
  import MapViewConstants.*
  import scalafx.scene.control.Label

  private val mapWidth = width
  private val mapHeight = height
  private val gridPane = new GridPane
  private val toolsGroup = new ToggleGroup
  private val toolButtons = createToolButtons()
  private val buttons = createGrid(DefaultCellSize)

  private val alert = new Alert(AlertType.Error):
    title = "Error"

  private def sidebar: BorderPane =
    new BorderPane:
      prefWidth = 200
      padding = DefaultPadding
      margin = Insets(10)

      top = new VBox:
        spacing = DefaultSpacing
        children = toolButtons

      bottom = parseMapButton

  right = sidebar
  center = gridPane
  maxWidth = 1300

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

  private def parseMapButton: Button = new Button("Parse Map"):
    maxWidth = Double.MaxValue
    onAction = _ => controller.onNext(mapWidth, mapHeight)

  private def createToolButtons(): Seq[Node] =
    val railLabel = new Label("Rails:")

    val rails = railNameToCell.keys.toSeq.map { label =>
      new RadioButton(label):
        toggleGroup = toolsGroup
    }

    val stationLabel = new Label("Stations:")
    val stations = stationNameToCell.keys.toSeq.map { label =>
      new RadioButton(label):
        toggleGroup = toolsGroup
    }

    rails ++ stations match
      case allButtons =>
        Seq(railLabel) ++ rails ++ Seq(stationLabel) ++ stations

  private def setupToolListener(): Unit =
    toolsGroup.selectedToggle.onChange { (_, _, newToggle) =>
      Option(newToggle)
        .collect { case rb: javafx.scene.control.RadioButton => RadioButton(rb) }
        .flatMap(rb => railNameToCell.get(rb.text()).orElse(stationNameToCell.get(rb.text())))
        .foreach(controller.selectTool)
    }

  private def setupModelListener(): Unit =
    controller.setOnModelUpdated { model =>
      Platform.runLater:
        for y <- 0 until height; x <- 0 until width do
          val cell = model.cells(y)(x)
          buttons(y)(x).style = toCssColor(cellToColor.getOrElse(cell.cellType, DefaultColor))
    }

  private def toCssColor(color: String): String = s"-fx-background-color: $color"

  def showError(error: ErrorMessage, msg: String = ""): Unit =
    Platform.runLater:
      alert.setContentText(s"$msg: $error")
      alert.showAndWait()

  override def getRoot: Parent = this
