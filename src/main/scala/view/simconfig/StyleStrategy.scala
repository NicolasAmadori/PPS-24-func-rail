package view.simconfig

import model.railway.{BigStation, MetalRail, SmallStation, TitaniumRail}

/**
 * Strategy trait for defining styles for vertices and edges in a graph.
 */
trait StyleStrategy:
  /**
   * Returns the CSS class name for a given vertex.
   *
   * @param vertex
   *   the vertex for which to get the style
   * @return
   *   the CSS class name as a String
   */
  def vertexStyle(vertex: StationView): String
  
  /**
   * Returns the CSS class name for a given edge.
   *
   * @param edge
   *   the edge for which to get the style
   * @return
   *   the CSS class name as a String
   */
  def edgeStyle(edge: RailView): String

/**
 * Default implementation of the StyleStrategy trait.
 * It provides basic styles for vertices and edges based on their types.
 */
object DefaultStyleStrategy extends StyleStrategy:
  def vertexStyle(vertex: StationView): String = vertex.stationObject match
    case _: SmallStation => "small-vertex"
    case _: BigStation => "big-vertex"
    case _ => "vertex"

  def edgeStyle(edge: RailView): String =
    edge.railObject match
      case _: MetalRail => "small-edge"
      case _: TitaniumRail => "big-edge"
      case _ => "edge"
