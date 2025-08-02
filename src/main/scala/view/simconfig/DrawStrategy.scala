package view.simconfig

import model.railway.{BigStation, MetalRail, SmallStation, TitaniumRail}

trait StyleStrategy:
  def vertexStyle(vertex: StationView): String
  def edgeStyle(edge: RailView): String

object DefaultStyleStrategy extends StyleStrategy:
  def vertexStyle(vertex: StationView): String = vertex.stationObject match
    case _: SmallStation => "small-vertex"
    case _: BigStation => "big-vertex"
    case _ => "vertex"

  def edgeStyle(edge: RailView): String = {
    println(s"Applying style to edge: ${edge.railCode}")
    edge.railObject match
      case _: MetalRail => "small-edge"
      case _: TitaniumRail => "big-edge"
      case _ => "edge"
  }
