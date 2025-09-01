package view.mapbuilder

import model.mapgrid.*
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, CheckBox, Label, RadioButton, TextField, ToggleGroup}
import scalafx.scene.layout.{BorderPane, GridPane, HBox, VBox}
import scalafx.scene.{Node, Parent}
import utils.{CustomError, ErrorMessage}
import view.simconfig.SimulationConfigViewConstants.{DefaultPadding, DefaultSpacing}
import view.View
import MapViewConstants.*
import ToolMappings.*
import controller.mapbuilder.MapBuilderController
import model.util.RailwayMapper
import scalafx.geometry.Pos.Center

object ToolMappings:
  val eraserNameToCell: Map[String, CellType] = Map(
    "Eraser" -> EmptyType
  )

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

class MapBuilderView(width: Int, height: Int, controller: MapBuilderController) extends BorderPane, View:

  private val UnexpectedErrorText = "Unexpected error"

  private val gridPane = new GridPane
  private val toolsGroup = new ToggleGroup
  private val toolButtons = createToolButtons()
  private val buttons = createGrid(DefaultCellSize)
  private val budgetLeft = new Label("Budget left: 0"):
    visible = false

  private val alert = new Alert(AlertType.Error):
    title = "Error"

  private def sidebar: BorderPane =
    new BorderPane:
      prefWidth = 200
      padding = DefaultPadding
      margin = Insets(10)

      top = new VBox:
        spacing = DefaultSpacing
        children = createBudgetBox() +: toolButtons

      bottom = parseMapButton

  right = sidebar
  center = gridPane
  maxWidth = 1300

  initialize()

  private def initialize(): Unit =
    setupModelListener()
    setupToolListener()

  import scalafx.Includes.*
  import scalafx.scene.input.{MouseEvent, MouseDragEvent}

  private var painting = false

  private def createGrid(cellSize: Int): Vector[Vector[Button]] =
    Vector.tabulate(height, width) { (row, col) =>
      val btn = new Button:
        minWidth = cellSize
        minHeight = cellSize
        maxWidth = cellSize
        maxHeight = cellSize
        style = toCssColor(DefaultColor)
        focusTraversable = false

      btn.onMousePressed = (_: MouseEvent) =>
        try
          controller.placeAt(col, row)
        catch
          case e => showError(CustomError(e.getMessage), UnexpectedErrorText)

      btn.onDragDetected = (e: MouseEvent) =>
        painting = true
        btn.startFullDrag()
        e.consume()

      btn.onMouseDragEntered = (_: MouseDragEvent) =>
        try
          if painting then controller.placeAt(col, row)
        catch
          case e => showError(CustomError(e.getMessage), UnexpectedErrorText)

      btn.onMouseReleased = (_: MouseEvent) =>
        painting = false

      gridPane.add(btn, col, row)
      btn
    }

  private def parseMapButton: Button = new Button("Parse Map"):
    maxWidth = Double.MaxValue
    onAction = _ =>
      try
        controller.onNext()
      catch
        case e => showError(CustomError(e.getMessage), UnexpectedErrorText)

  private def createToolButtons(): Seq[Node] =
    val instructions = new Label("Click or drag to place pieces")
    val railLabel = new Label("Rails (" + RailwayMapper.BLOCK_TO_KM_MULTIPLIER + " km each):")

    val eraser = new RadioButton("Eraser"):
      toggleGroup = toolsGroup

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
        List(instructions) ++ List(eraser) ++ (Seq(railLabel) ++ rails ++ Seq(stationLabel) ++ stations)

  private def createBudgetBox(): Node =
    val textField = new TextField():
      promptText = "Budget"
      disable = true
      onKeyTyped = _ =>
        try
          controller.setBudget(text.value.toIntOption.getOrElse(0))
        catch
          case e => showError(CustomError(e.getMessage), UnexpectedErrorText)

    val checkbox = new CheckBox():
      onAction = _ =>
        if selected.value then
          textField.disable = false
        else
          textField.disable = true
          textField.text = ""
          controller.disableBudget()

    val budgetControls = new HBox(checkbox, textField):
      alignment = Center
      spacing = 5

    new VBox(budgetControls, budgetLeft):
      spacing = 5
      style = "-fx-stroke-width: 1; -fx-border-color: gray; -fx-padding: 5"

  private def setupToolListener(): Unit =
    toolsGroup.selectedToggle.onChange { (_, _, newToggle) =>
      Option(newToggle)
        .collect { case rb: javafx.scene.control.RadioButton => RadioButton(rb) }
        .flatMap(rb =>
          railNameToCell.get(rb.text()).orElse(stationNameToCell.get(rb.text())).orElse(eraserNameToCell.get(rb.text()))
        )
        .foreach(controller.selectTool)
    }

  private def setupModelListener(): Unit =
    controller.setOnModelUpdated { model =>
      Platform.runLater:
        for y <- 0 until height; x <- 0 until width do
          val cell = model.cells(y)(x)
          buttons(y)(x).style = toCssColor(cellToColor.getOrElse(cell.cellType, DefaultColor))
        model.budget match
          case Some(b) =>
            budgetLeft.visible = true
            budgetLeft.text = "Budget left: " + b
          case _ => budgetLeft.visible = false
    }

  private def toCssColor(color: String): String = s"-fx-background-color: $color"

  def showError(error: ErrorMessage, title: String): Unit =
    if !painting then
      Platform.runLater:
        alert.contentText = error.toString
        alert.headerText = title
        alert.showAndWait()

  override def getRoot: Parent = this
