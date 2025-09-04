package view.simconfig

import com.brunomnsilva.smartgraph.graph.{Edge, Graph, Vertex}
import com.brunomnsilva.smartgraph.graphview.{SmartCircularSortedPlacementStrategy, SmartGraphPanel, SmartGraphProperties, SmartStylableNode}
import javafx.beans.property.BooleanProperty
import scalafx.scene.Parent
import scalafx.scene.layout.StackPane
import view.util.Converters

import java.io.InputStream
import java.net.URI

/** GraphView is a wrapper around SmartGraphPanel to provide a view for a graph.
  *
  * @param graph
  *   the graph to view
  * @tparam V
  *   vertices type
  * @tparam E
  *   edges type
  */
class GraphView[V, E](graph: Graph[V, E]):

  private val DEFAULT_STYLE_FILE = "/smartgraph.css"
  private val DEFAULT_PROPERTIES_FILE = "/smartgraph.properties"

  val graphView: SmartGraphPanel[V, E] = new SmartGraphPanel[V, E](
    graph,
    SmartGraphProperties(loadPropertiesFile),
    new SmartCircularSortedPlacementStrategy(),
    getStylesheetUri
  )

  val automaticLayoutProperty: BooleanProperty = graphView.automaticLayoutProperty()
  val edges: List[Edge[E, V]] = Converters.toImmutableList(graphView.getModel.edges().stream().toList)
  val vertices: List[Vertex[V]] = Converters.toImmutableList(graphView.getModel.vertices().stream().toList)

  /** Initializes the graph view */
  def init(): Unit =
    graphView.init()

  /** Sets the automatic layout property */
  def setAutomaticLayout(value: Boolean): Unit =
    graphView.setAutomaticLayout(value)

  /** Gets the edges as stylable nodes */
  def getStylableEdge(edge: Edge[E, V]): SmartStylableNode =
    graphView.getStylableEdge(edge)

  /** Gets the vertices as stylable nodes */
  def getStylableVertex(vertex: Vertex[V]): SmartStylableNode =
    graphView.getStylableVertex(vertex)

  /** Gets the graph pane */
  def getView: Parent = new StackPane:
    children.add(graphView)

  /** Gets the graph pane */
  def getView(screenWidth: Int): Parent = new StackPane:
    prefWidth = screenWidth
    children.add(graphView)

  private def getStylesheetUri: URI = getClass.getResource(DEFAULT_STYLE_FILE).toURI

  private def loadPropertiesFile: InputStream = getClass.getResourceAsStream(DEFAULT_PROPERTIES_FILE)
