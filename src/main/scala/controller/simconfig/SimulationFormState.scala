package controller.simconfig

import model.railway.Domain.StationCode

sealed trait TrainType
object HighSpeed extends TrainType
object NormalSpeed extends TrainType

/** Configuration for a train in the simulation.
  * @param id
  *   unique identifier for the train
  * @param name
  *   name of the train
  * @param trainType
  *   type of the train (HighSpeed or NormalSpeed)
  * @param departureStation
  *   the station from which the train departs
  * @param stops
  *   list of stations where the train stops
  */
case class TrainConfig(
    id: Int = 0,
    name: String = "",
    trainType: TrainType = NormalSpeed,
    departureStation: StationCode = StationCode.empty,
    stops: List[StationCode] = List.empty
)

/** Companion object of TrainConfig */
object TrainConfig:
  /** Factory method to create a TrainConfig with a specific id */
  def apply(id: Int): TrainConfig = new TrainConfig(id)

/** State of the simulation form, containing a list of trains. This handles automatic ID generation for new trains and
  * provides methods to manage them.
  * @param trains
  * @param trainId
  *   unique identifier for the next train to be added
  */
case class SimulationFormState(trains: List[TrainConfig] = List.empty, trainId: Int = 0):
  /** @return
    *   the last used train ID, or 0 if no trains exist
    */
  def getLastId: Int = if trains.isEmpty then 0 else trains.last.id

  /** Adds a new train to the simulation form state.
    * @return
    *   a tuple containing the ID of the new train and the updated SimulationFormState
    */
  def addTrain(): (Int, SimulationFormState) =
    val nextId = trainId + 1
    (trainId, copy(trains = trains :+ TrainConfig(trainId), trainId = nextId))

  /** Renames a train by ID */
  def updateTrainName(id: Int, name: String): SimulationFormState =
    copy(trains = trains.map {
      case train if train.id == id => train.copy(name = name)
      case train => train
    })

  /** Sets the train type to HighSpeed by ID */
  def setHighSpeedTrain(id: Int): SimulationFormState =
    copy(trains = trains.map {
      case train if train.id == id => train.copy(trainType = HighSpeed)
      case train => train
    })

  /** Sets the train type to NormalSpeed by ID */
  def setNormalSpeedTrain(id: Int): SimulationFormState =
    copy(trains = trains.map {
      case train if train.id == id => train.copy(trainType = NormalSpeed)
      case train => train
    })

  /** Sets the departure station for a train by ID */
  def setDepartureStation(id: Int, station: StationCode): SimulationFormState =
    copy(trains = trains.map {
      case train if train.id == id => train.copy(departureStation = station)
      case train => train
    })

  /** Adds a stop to a train by ID */
  def addStop(id: Int, station: StationCode): SimulationFormState =
    copy(trains = trains.map {
      case train if train.id == id => train.copy(stops = train.stops :+ station)
      case train => train
    })

  /** Removes a stop from a train by ID */
  def removeStop(id: Int, station: StationCode): SimulationFormState =
    copy(trains = trains.map {
      case train if train.id == id => train.copy(stops = train.stops.filterNot(_ == station))
      case train => train
    })
