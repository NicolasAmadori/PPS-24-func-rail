package view

import com.brunomnsilva.smartgraph.graph.{Edge, Graph, Vertex}
import com.brunomnsilva.smartgraph.graphview.{SmartCircularSortedPlacementStrategy, SmartGraphPanel, SmartStylableNode}
import javafx.beans.property.BooleanProperty
import scalafx.scene.Parent
import scalafx.scene.layout.StackPane
import utils.Converters

class GraphView[V, E](graph: Graph[V, E]):

  val graphView: SmartGraphPanel[V, E] = new SmartGraphPanel[V, E](graph, new SmartCircularSortedPlacementStrategy())

  val automaticLayoutProperty: BooleanProperty = graphView.automaticLayoutProperty()
  val edges: List[Edge[E, V]] = Converters.toImmutableList(graphView.getModel.edges().stream().toList)
  val vertices: List[Vertex[V]] = Converters.toImmutableList(graphView.getModel.vertices().stream().toList)

  def init(): Unit =
    graphView.init()

  def setAutomaticLayout(value: Boolean): Unit =
    graphView.setAutomaticLayout(value)

  def getStylableEdge(edge: Edge[E, V]): SmartStylableNode =
    graphView.getStylableEdge(edge)

  def getStylableVertex(vertex: Vertex[V]): SmartStylableNode =
    graphView.getStylableVertex(vertex)

  def getView: Parent = new StackPane:
    children.add(graphView)
