package view.simconfig

import com.brunomnsilva.smartgraph.graph.{Edge, Graph, Vertex}
import com.brunomnsilva.smartgraph.graphview.{SmartCircularSortedPlacementStrategy, SmartGraphPanel, SmartStylableNode}
import javafx.beans.property.BooleanProperty
import scalafx.scene.Parent
import scalafx.scene.layout.StackPane
import utils.Converters

/** GraphView is a wrapper around SmartGraphPanel to provide a view for a graph.
  * @param graph
  * @tparam V
  *   vertices type
  * @tparam E
  *   edges type
  */
class GraphView[V, E](graph: Graph[V, E]):

  val graphView: SmartGraphPanel[V, E] = new SmartGraphPanel[V, E](graph, new SmartCircularSortedPlacementStrategy())

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
