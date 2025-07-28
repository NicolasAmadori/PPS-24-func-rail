package model.railway

import model.railway.Domain.StationCode

trait Station:
  def code: StationCode
  def trains: List[Train]

case class SmallStation(code: StationCode, trains: List[Train] = List.empty) extends Station

case class BigStation(code: StationCode, trains: List[Train] = List.empty) extends Station

object Station:
  def bigStation(code: String): BigStation = BigStation(StationCode.fromString(code))

  def smallStation(code: String): SmallStation = SmallStation(StationCode.fromString(code))
