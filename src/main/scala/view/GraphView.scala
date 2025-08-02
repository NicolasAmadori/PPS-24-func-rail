package view

import com.brunomnsilva.smartgraph.graph.{Edge, Graph, Vertex}
import com.brunomnsilva.smartgraph.graphview.{SmartCircularSortedPlacementStrategy, SmartGraphEdge, SmartGraphPanel, SmartPlacementStrategy, SmartStylableNode}
import javafx.beans.property.BooleanProperty
import scalafx.scene.layout.Region
import javafx.scene.layout.Region as JFXRegion
import utils.Converters
import view.simconfig.{DefaultStyleStrategy, StyleStrategy}

import java.util
import java.util.stream.Collectors
import scala.jdk.javaapi.CollectionConverters

class GraphView[V, E](graph: Graph[V, E]) extends Region:

  val graphView: SmartGraphPanel[V, E] = new SmartGraphPanel[V, E](graph, new SmartCircularSortedPlacementStrategy())

  override val delegate: JFXRegion = graphView

  val automaticLayoutProperty: BooleanProperty = graphView.automaticLayoutProperty()
  val edges: List[Edge[E, V]] = Converters.toImmutableList(graphView.getModel.edges().stream().toList)
  val vertices: List[Vertex[V]] = Converters.toImmutableList(graphView.getModel.vertices().stream().toList)

  def init: Unit =
    graphView.init()

  def setAutomaticLayout(value: Boolean): Unit =
    graphView.setAutomaticLayout(value)
    
  def getStylableEdge(edge: Edge[E, V]): SmartStylableNode =
    graphView.getStylableEdge(edge)
  
  def getStylableVertex(vertex: Vertex[V]): SmartStylableNode =
    graphView.getStylableVertex(vertex)