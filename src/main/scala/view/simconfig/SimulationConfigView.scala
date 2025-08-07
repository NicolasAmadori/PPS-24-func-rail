package view.simconfig

import controller.simconfig.SimulationConfigController
import model.railway.Domain.StationCode
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.geometry.Pos.{BottomCenter, Center}

import scalafx.scene.control.*
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.ScrollPane.ScrollBarPolicy.{AsNeeded, Never}
import scalafx.scene.layout.*
import utils.ErrorMessage
import view.{GraphUtil, View}

object SimulationConfigViewConstants:
  val DefaultWindowMinWidth = 1300
  val DefaultWindowMinHeight = 800
  val SidebarMinWidth = 350
  val DefaultSpacing = 10
  val DefaultPadding: Insets = Insets(10)
  val SmallSpacing = 5

class SimulationConfigView(
    controller: SimulationConfigController,
    styleStrategy: StyleStrategy = DefaultStyleStrategy
) extends View:

  import SimulationConfigViewConstants.*

  private val stations = controller.getStationCodes

  private val alert = new Alert(AlertType.Error):
    title = "Error"

  private val graphView = GraphView[StationView, RailView](GraphUtil.createGraph())
  private val root = createRoot
  private lazy val sidebarContainer: Pane = new VBox():
    prefWidth = SidebarMinWidth
    spacing = DefaultSpacing

  private var sidebarGroups: List[Pane] = List.empty

  private def applyGraphStyling(): Unit =
    graphView.edges.foreach(e => graphView.getStylableEdge(e).addStyleClass(styleStrategy.edgeStyle(e.element())))
    graphView.vertices.foreach(v =>
      graphView.getStylableVertex(v).addStyleClass(styleStrategy.vertexStyle(v.element()))
    )

  def initGraph(): Unit =
    graphView.setAutomaticLayout(true)
    applyGraphStyling()
    graphView.init()

  def getRoot: Pane = root

  private def createRoot: Pane =
    new BorderPane:
      minWidth = DefaultWindowMinWidth
      minHeight = DefaultWindowMinHeight
      center = graphPane
      right = sidebar

  private def sidebar: VBox =
    new VBox:
      prefWidth = SidebarMinWidth
      padding = DefaultPadding
      spacing = DefaultSpacing
      children = Seq(
        newTrainButton,
        trainConfigScrollPane,
        startSimulationButton
      )

  private def newTrainButton: Button = new Button("New train"):
    onAction = _ =>
      val id = controller.addTrain()
      sidebarGroups = sidebarGroups ++ List(TrainConfigGroup(id, stations, controller))
      sidebarContainer.children = sidebarGroups

  private def trainConfigScrollPane: ScrollPane = new ScrollPane():
    hbarPolicy = Never
    vbarPolicy = AsNeeded
    fitToWidth = true
    style = "-fx-border-color: transparent; -fx-background-insets: 0;"
    content = sidebarContainer

  private def startSimulationButton: Button = new Button("Start simulation"):
    alignmentInParent = BottomCenter
    maxWidth = Double.MaxValue
    onAction = _ => controller.startSimulation()

  private def graphPane: Pane =
    val automaticLayoutCheckbox = CheckBox("Automatic layout")
    automaticLayoutCheckbox.selectedProperty().bindBidirectional(graphView.automaticLayoutProperty)

    val automaticLayoutHBox = new HBox(automaticLayoutCheckbox):
      alignment = Center
      padding = Insets(10)
      spacing = 10

    new BorderPane:
      center = graphView.getView
      bottom = automaticLayoutHBox

  def showError(error: ErrorMessage, msg: String = ""): Unit =
    alert.setContentText(s"$msg: $error")
    alert.show()

  def showErrors(errors: List[ErrorMessage], title: String = ""): Unit =
    val error = errors.mkString("\n")
    sidebarContainer.children = List(
      ErrorBox(error, title)
    ) ++ sidebarGroups

class TrainConfigGroup(trainId: Int, stations: List[StationCode], controller: SimulationConfigController) extends VBox:
  import SimulationConfigViewConstants.*

  spacing = SmallSpacing
  style = "-fx-stroke-width: 1; -fx-border-color: gray; -fx-padding: 5"

  private val trainNameTextField =
    new TextField:
      promptText = "Train name"
      onKeyTyped = _ => controller.updateTrainName(trainId, text.value)

  private val highSpeedCheckBox =
    new CheckBox("High speed"):
      onAction = _ =>
        if selected.value then
          controller.setHighSpeedTrain(trainId)
        else
          controller.setNormalSpeedTrain(trainId)

  private val stationsComboBox =
    new ComboBox[String]:
      promptText = "Select departure station"
      items = ObservableBuffer(stations.map(s => StationCode.value(s))*)
      onAction = _ =>
        val selectedStation = value.value
        if selectedStation != null then
          controller.setDepartureStation(trainId, StationCode.fromString(selectedStation))

  private val stopsCheckBoxes =
    stations.map(s => StationCode.value(s)).map { station =>
      new CheckBox(station):
        selected = false
        onAction = _ =>
          if selected.value then
            controller.addStop(trainId, StationCode.fromString(station))
          else
            controller.removeStop(trainId, StationCode.fromString(station))
    }

  private def stopsCheckboxes: ScrollPane =
    new ScrollPane():
      maxHeight = 300
      content = new VBox():
        spacing = SmallSpacing
        padding = Insets(5)
        children = List(new Label("Select stops")) ++ stopsCheckBoxes

  private def sideBySide(node: Region*): HBox =
    new HBox:
      spacing = SmallSpacing
      children = node.toSeq

  children = Seq(
    sideBySide(trainNameTextField, highSpeedCheckBox),
    stationsComboBox,
    stopsCheckboxes
  )

class ErrorBox(message: String, title: String = "") extends HBox:
  import SimulationConfigViewConstants.*

  style = "-fx-border-color: red; -fx-padding: 10; -fx-background-color: #ff0017;"
  padding = DefaultPadding
  spacing = SmallSpacing
  alignment = Center

  private val text = if title.isEmpty then message else s"$title: $message"

  private val errorLabel = new Label(text):
    style = "-fx-text-fill: white;"

  private val closeButton = new Button("X"):
    prefWidth = 30
    prefHeight = 30
    style = "-fx-background-color: transparent; -fx-text-fill: white;"

  children = Seq(errorLabel, closeButton)
