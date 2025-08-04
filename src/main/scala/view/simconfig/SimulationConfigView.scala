package view.simconfig

import scalafx.geometry.Insets
import scalafx.geometry.Pos.Center
import scalafx.scene.control.{Button, CheckBox}
import scalafx.scene.layout.{BorderPane, HBox, Pane, VBox}
import view.GraphUtil
import view.simconfig.{DefaultStyleStrategy, GraphView, RailView, StationView, StyleStrategy}

class SimulationConfigView(
    styleStrategy: StyleStrategy = DefaultStyleStrategy
):

  private val graphView = GraphView[StationView, RailView](GraphUtil.createGraph())
  graphView.setAutomaticLayout(true)
  graphView.edges.foreach(e => graphView.getStylableEdge(e).addStyleClass(styleStrategy.edgeStyle(e.element())))
  graphView.vertices.foreach(v => graphView.getStylableVertex(v).addStyleClass(styleStrategy.vertexStyle(v.element())))

  def initGraph(): Unit =
    graphView.init()

  def getRoot: Pane =
    new BorderPane:
      minWidth = 1000
      minHeight = 800
      center = graphBox
      right = configurationSidebar

  private def configurationSidebar: Pane =
    val button = Button("Placeholder button")
    val vBox = new VBox(button):
      minWidth = 300

    vBox

  private def graphBox: Pane =
    val automaticLayoutCheckbox = CheckBox("Automatic layout")
    automaticLayoutCheckbox.selectedProperty().bindBidirectional(graphView.automaticLayoutProperty)

    val automaticLayoutHBox = new HBox(automaticLayoutCheckbox):
      alignment = Center
      padding = Insets(10)
      spacing = 10

    new BorderPane:
      center = graphView.getView
      bottom = automaticLayoutHBox
