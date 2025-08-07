package model.railway

import model.railway.Domain.StationCode

trait Station:
  def code: StationCode

case class SmallStation(code: StationCode) extends Station

case class BigStation(code: StationCode) extends Station

object Station:
  def bigStation(code: String): Station = BigStation(StationCode.fromString(code))

  def smallStation(code: String): Station = SmallStation(StationCode.fromString(code))
