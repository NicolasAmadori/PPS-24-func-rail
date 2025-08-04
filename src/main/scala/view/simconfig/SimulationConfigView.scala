package view.simconfig

import scalafx.geometry.Insets
import scalafx.geometry.Pos.Center
import scalafx.scene.control.{Button, CheckBox}
import scalafx.scene.layout.{BorderPane, HBox, Pane, VBox}
import view.{GraphUtil, View}

object SimulationConfigViewConstants:
  val DefaultWindowMinWidth = 1000
  val DefaultWindowMinHeight = 800
  val SidebarMinWidth = 300

class SimulationConfigView(
    styleStrategy: StyleStrategy = DefaultStyleStrategy
) extends View:

  import SimulationConfigViewConstants.*

  private val graphView = GraphView[StationView, RailView](GraphUtil.createGraph())
  private val root = createRoot

  def initGraph(): Unit =
    graphView.setAutomaticLayout(true)
    graphView.edges.foreach(e => graphView.getStylableEdge(e).addStyleClass(styleStrategy.edgeStyle(e.element())))
    graphView.vertices.foreach(v =>
      graphView.getStylableVertex(v).addStyleClass(styleStrategy.vertexStyle(v.element()))
    )
    graphView.init()

  def getRoot: Pane = root

  private def createRoot: Pane =
    new BorderPane:
      minWidth = DefaultWindowMinWidth
      minHeight = DefaultWindowMinHeight
      center = graphPane
      right = sidebar

  private def sidebar: Pane =
    val button = Button("Placeholder button")
    val vBox = new VBox(button):
      minWidth = SidebarMinWidth
    vBox

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
