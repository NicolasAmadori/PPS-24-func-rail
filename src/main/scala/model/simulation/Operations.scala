package model.simulation

trait TrainOperations[S <: TrainOperations[S]]:
  self: S =>
  def trains: List[Train]

  def withTrains(newTrains: List[Train]): S

  def addTrain(train: Train): S =
    if trains.contains(train) then self
    else withTrains(trains :+ train)
