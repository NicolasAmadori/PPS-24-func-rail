package view.simconfig

import com.brunomnsilva.smartgraph.graphview.SmartRadiusSource
import model.railway.Domain.{RailCode, StationCode}
import model.railway.{BigStation, Rail, Station}

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

  override def toString: String = rail.length.toString


