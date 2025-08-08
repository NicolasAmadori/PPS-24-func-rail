package model.railway

import model.railway.Domain.StationCode

trait Station:
  def code: StationCode

case class SmallStation(code: StationCode) extends Station

case class BigStation(code: StationCode) extends Station

object Station:
  def bigStation(code: String): BigStation = BigStation(StationCode(code))

  def smallStation(code: String): SmallStation = SmallStation(StationCode(code))
