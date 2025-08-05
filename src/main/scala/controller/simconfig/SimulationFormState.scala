package controller.simconfig

import model.railway.Domain.StationCode

sealed trait TrainType
object HighSpeed extends TrainType
object NormalSpeed extends TrainType

case class TrainConfig(
    id: Int = 0,
    name: String = "",
    trainType: TrainType = NormalSpeed,
    departureStation: StationCode = StationCode.empty,
    stops: List[StationCode] = List.empty
)

object TrainConfig:
  def apply(id: Int): TrainConfig = new TrainConfig(id)

case class SimulationFormState(trains: List[TrainConfig] = List.empty, trainId: Int = 0):
  def getLastId: Int = if trains.isEmpty then 0 else trains.last.id

  def addTrain(): (Int, SimulationFormState) =
    val nextId = trainId + 1
    (trainId, copy(trains = trains :+ TrainConfig(trainId), trainId = nextId))

  def updateTrainName(id: Int, name: String): SimulationFormState =
    copy(trains = trains.map {
      case train if train.id == id => train.copy(name = name)
      case train => train
    })

  def setHighSpeedTrain(id: Int): SimulationFormState =
    copy(trains = trains.map {
      case train if train.id == id => train.copy(trainType = HighSpeed)
      case train => train
    })

  def setNormalSpeedTrain(id: Int): SimulationFormState =
    copy(trains = trains.map {
      case train if train.id == id => train.copy(trainType = NormalSpeed)
      case train => train
    })

  def setDepartureStation(id: Int, station: StationCode): SimulationFormState =
    copy(trains = trains.map {
      case train if train.id == id => train.copy(departureStation = station)
      case train => train
    })

  def addStop(id: Int, station: StationCode): SimulationFormState =
    copy(trains = trains.map {
      case train if train.id == id => train.copy(stops = train.stops :+ station)
      case train => train
    })

  def removeStop(id: Int, station: StationCode): SimulationFormState =
    copy(trains = trains.map {
      case train if train.id == id => train.copy(stops = train.stops.filterNot(_ == station))
      case train => train
    })
