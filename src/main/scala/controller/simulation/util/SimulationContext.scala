package controller.simulation.util

import model.entities.{Itinerary, Passenger, PassengerState, Route}
import model.simulation.{Simulation, TrainPosition}

case class SimulationContext(
    routes: List[Route] = List.empty,
    trainHistories: List[List[TrainPosition]] = List.empty,
    itineraries: List[Itinerary] = List.empty,
    passengers: List[Passenger] = List.empty,
    passengerStates: List[PassengerState] = List.empty
)

object SimulationContext:
  def from(sim: Simulation): SimulationContext =
    SimulationContext(
      routes = sim.state.trains.map(_.route),
      trainHistories = sim.state.trainStates.values.map(_.previousPositions).toList,
      itineraries = sim.state.passengers.flatMap(_.itinerary),
      passengers = sim.state.passengers,
      passengerStates = sim.state.passengerStates.values.toList
    )
