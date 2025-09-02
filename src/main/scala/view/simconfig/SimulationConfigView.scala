package view.simconfig

import controller.simconfig.SimulationConfigController
import model.entities.EntityCodes.StationCode
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.geometry.Pos.{BottomCenter, Center}
import scalafx.scene.control.*
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.ScrollPane.ScrollBarPolicy.{AsNeeded, Never}
import scalafx.scene.layout.*
import utils.{CustomError, ErrorMessage}
import view.{GraphUtil, View}

import scala.language.postfixOps

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

  private val UnexpectedErrorText = "Unexpected error"

  private val graphView = GraphView[StationView, RailView](GraphUtil.createGraph(controller.getRailway))
  private val stations = controller.getStationCodes

  private var simulationDuration: Int = 100
  private var simulationSpeed: Int = 60
  private var faultsEnabled: Boolean = false
  private val STRATEGIES: Map[String, Int] =
    Map.from(List(("Random", 0), ("Shortest time", 1), ("Shortest distance", 2), ("Lowest cost", 3)))
  private var itineraryStrategy: Int = STRATEGIES.values.head

  private val startButton = startSimulationButton

  private val alert = new Alert(AlertType.Error):
    title = "Error"

  private val root = createRoot
  private lazy val sidebarContainer: VBox = new VBox():
    prefWidth = SidebarMinWidth
    spacing = DefaultSpacing
    vgrow = Priority.Always

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

  private def sidebar: BorderPane =
    new BorderPane:
      prefWidth = SidebarMinWidth
      padding = DefaultPadding

      top = new VBox:
        spacing = DefaultSpacing
        children = Seq(
          new SimulationConfigGroup,
          newTrainButton,
          new VBox:
            vgrow = Priority.Always
            children = Seq(trainConfigScrollPane)
        )
      bottom = bottomConfigContainer

  private def bottomConfigContainer: VBox =
    new VBox():
      alignment = BottomCenter
      children = List(
        new HBox:
          spacing = DefaultSpacing
          padding = Insets(10, 0, 0, 0)
          fillHeight = true
          alignment = BottomCenter
          maxWidth = Double.MaxValue
          children = Seq(
            backButton,
            startButton
          )
      )

  private def newTrainButton: Button = new Button("New train"):
    prefWidth = SidebarMinWidth
    onAction = _ =>
      val id = controller.addTrain()
      var trainGroup: TrainConfigGroup = null
      trainGroup = TrainConfigGroup(
        id,
        stations,
        controller,
        () =>
          sidebarGroups = sidebarGroups.filterNot(_ eq trainGroup)
          sidebarContainer.children = sidebarGroups
          controller.removeTrain(id)
      )
      sidebarGroups = sidebarGroups :+ trainGroup
      sidebarContainer.children = sidebarGroups
      sidebarContainer.requestLayout()

  private def trainConfigScrollPane: ScrollPane = new ScrollPane():
    hbarPolicy = Never
    vbarPolicy = AsNeeded
    fitToWidth = true
    style = "-fx-border-color: transparent; -fx-background-insets: 0;"
    content = sidebarContainer
    maxHeight = 570

  private def startSimulationButton: Button = new Button("Start simulation"):
    prefWidth = SidebarMinWidth - 70
    onAction = _ =>
      graphView.automaticLayoutProperty.set(true)
      try
        controller.startSimulation(simulationDuration, simulationSpeed, faultsEnabled, itineraryStrategy, graphView)
      catch
        case e => showError(CustomError(e.getMessage), UnexpectedErrorText)

  private def backButton: Button = new Button("Back"):
    prefWidth = 70
    onAction = _ => controller.onBack()

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
      ErrorBox(
        error,
        title,
        () =>
          sidebarContainer.children.remove(0)
      )
    ) ++ sidebarGroups

  class SimulationConfigGroup extends VBox:
    import SimulationConfigViewConstants.*

    spacing = DefaultSpacing
    padding = DefaultPadding
    style = "-fx-stroke-width: 1; -fx-border-color: gray; -fx-padding: 10"

    private val faultsCheckBox =
      new CheckBox("Enable faults"):
        onAction = _ => faultsEnabled = this.selected.value

    private val itineraryStrategyComboBox =
      new VBox:
        spacing = SmallSpacing
        alignment = Center
        children = Seq(
          new Label("Passenger itinerary strategy"),
          new ComboBox[String]:
            promptText = "Select itinerary strategy"
            items = ObservableBuffer.from(STRATEGIES.keys)
            value = STRATEGIES.keySet.head
            onAction = _ =>
              itineraryStrategy = STRATEGIES(value.value)
        )

    private val topRow =
      new HBox:
        spacing = DefaultSpacing
        alignment = Center
        children = Seq(faultsCheckBox, itineraryStrategyComboBox)

    private val durationBox =
      new VBox:
        spacing = SmallSpacing
        alignment = Center
        children = Seq(
          new Label("Simulation duration"),
          new HBox:
            spacing = SmallSpacing
            alignment = Center
            children = Seq(
              new TextField:
                maxWidth = 50
                promptText = "Simulation duration"
                text = simulationDuration.toString
                onKeyTyped = _ =>
                  simulationDuration = text.value.toIntOption.getOrElse(0)
                  startButton.disable =
                    text.value.isEmpty || text.value.toIntOption.isEmpty || text.value.toInt <= 0
              ,
              new Label("(Days)")
            )
        )

    private val simulationSpeedSlider =
      new Slider(60, 600, simulationSpeed):
        blockIncrement = 5
        snapToTicks = false
        maxWidth = 200

    simulationSpeedSlider.valueProperty.addListener { (_, _, newValue) =>
      simulationSpeed = newValue.intValue
    }

    private val simulationSpeedBox =
      new VBox:
        spacing = SmallSpacing
        alignment = Center
        children = Seq(
          new Label("Simulation speed"),
          simulationSpeedSlider
        )

    private val durationAndSpeedRow =
      new HBox:
        spacing = DefaultSpacing
        alignment = Center
        children = Seq(durationBox, simulationSpeedBox)

    children = Seq(
      topRow,
      durationAndSpeedRow
    )

class TrainConfigGroup(
    trainId: Int,
    stations: List[StationCode],
    controller: SimulationConfigController,
    deletionCallable: () => Unit
) extends VBox:
  import SimulationConfigViewConstants.*

  spacing = SmallSpacing
  style = "-fx-stroke-width: 1; -fx-border-color: gray; -fx-padding: 5"

  controller.updateTrainName(trainId, s"T$trainId")

  private var departureStation: Option[String] = None

  private val trainNameTextField =
    new TextField:
      promptText = "Train name"
      text = s"T$trainId"
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
          departureStation = Some(selectedStation)
          controller.setDepartureStation(trainId, StationCode(selectedStation))
          stopsCheckBoxes.foreach(c => c.selected = c.text.value == selectedStation)

  private val stopsCheckBoxes =
    stations.map(s => StationCode.value(s)).map { station =>
      new CheckBox(station):
        selected = false
        onAction = _ =>
          if selected.value then
            controller.addStop(trainId, StationCode(station))
          else if station != departureStation.getOrElse("") then
            controller.removeStop(trainId, StationCode(station))
          else
            this.selected = true
    }

  private def stopsCheckboxes: ScrollPane =
    new ScrollPane():
      maxHeight = 150
      content = new VBox():
        spacing = SmallSpacing
        padding = Insets(5)
        children = List(new Label("Select stops")) ++ stopsCheckBoxes

  private def sideBySide(node: Region*): HBox =
    new HBox:
      spacing = SmallSpacing
      children = node.toSeq

  private val deleteButton =
    new Button("Delete train"):
      style = "-fx-text-fill: red;"
      onAction = _ => deletionCallable()

  children = Seq(
    sideBySide(trainNameTextField, highSpeedCheckBox),
    stationsComboBox,
    stopsCheckboxes,
    deleteButton
  )

class ErrorBox(message: String, title: String = "", deletionCallable: () => Unit) extends HBox:
  import SimulationConfigViewConstants.*

  style = "-fx-border-color: red; -fx-padding: 10; "
  padding = DefaultPadding
  spacing = SmallSpacing
  alignment = Center

  private val text = if title.isEmpty then message else s"$title: $message"

  private val errorLabel = new Label(text):
    style = "-fx-text-fill: red;"

  private val closeButton = new Button("Close"):
    style = "-fx-text-fill: black;"
    onAction = _ => deletionCallable()

  children = Seq(errorLabel, closeButton)
