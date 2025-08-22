package view.simconfig

import com.brunomnsilva.smartgraph.graphview.SmartRadiusSource
import model.entities.EntityCodes.{RailCode, StationCode}
import model.entities.{BigStation, Rail, Station, MetalRail, TitaniumRail}

class StationView(station: Station):

  def stationCode: StationCode = station.code
  def stationObject: Station = station

  override def toString: String = StationCode.value(stationCode)

  @SmartRadiusSource
  def modelRadius: Double = station match
    case _: BigStation => 20.0
    case _ => 10.0

class RailView(rail: Rail):

  def railCode: RailCode = rail.code
  def railObject: Rail = rail

  override def toString: String =
    rail.match
      case MetalRail(_, _, _, _) => "" + rail.code + " (" + rail.length.toString + ")"
      case TitaniumRail(_, _, _, _) => "" + rail.code + " (" + rail.length.toString + ")"
