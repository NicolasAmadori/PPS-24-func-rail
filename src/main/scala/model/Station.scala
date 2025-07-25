package model

import model.Domain.StationCode

trait Station:
  def code: StationCode
  def trains: List[Train]

case class SmallStation(code: StationCode, trains: List[Train] = List.empty) extends Station
    with TrainOperations[SmallStation]:
  override def withTrains(newTrains: List[Train]): SmallStation = copy(trains = newTrains)

case class BigStation(code: StationCode, trains: List[Train] = List.empty) extends Station
    with TrainOperations[BigStation]:
  override def withTrains(newTrains: List[Train]): BigStation = copy(trains = newTrains)

trait TrainOperations[S <: Station]:
  self: S =>
  def withTrains(newTrains: List[Train]): S

  def addTrain(train: Train): S = withTrains(train :: trains)

  def removeTrain(train: Train): S = withTrains(trains.filterNot(_ == train))

object Station:
  def bigStation(code: String): BigStation = BigStation(StationCode.fromString(code))

  def smallStation(code: String): SmallStation = SmallStation(StationCode.fromString(code))
