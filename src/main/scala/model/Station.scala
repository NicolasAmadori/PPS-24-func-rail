package model

import model.Domain.StationCode

trait Station:
  def code: StationCode

case class SmallStation(code: StationCode) extends Station

case class BigStation(code: StationCode) extends Station

object Station:
  def bigStation(code: String): BigStation = BigStation(StationCode.fromString(code))

  def smallStation(code: String): SmallStation = SmallStation(StationCode.fromString(code))
