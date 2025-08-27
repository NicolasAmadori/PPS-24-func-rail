package controller.simulation.util

import model.entities.EntityCodes.PassengerCode
import model.entities.PassengerPosition.AtStation
import model.entities.{Itinerary, Passenger, Route}
import model.simulation.{PassengerState, Simulation, TrainPosition}

case class SimulationContext(
    routes: List[Route] = List.empty,
    trainHistories: List[List[TrainPosition]] = List.empty,
    itineraries: List[Itinerary] = List.empty,
    passengers: List[Passenger] = List.empty,
    passengerStates: List[PassengerState] = List.empty,
    passengersWithCompletedTrip: List[PassengerState] = List.empty
)

object SimulationContext:
  def from(sim: Simulation): SimulationContext =
    SimulationContext(
      routes = sim.state.trains.map(_.route),
      trainHistories = sim.state.trainStates.values.map(_.previousPositions).toList,
      itineraries = sim.state.passengers.flatMap(_.itinerary),
      passengers = sim.state.passengers,
      passengerStates = sim.state.passengerStates.values.toList,
      passengersWithCompletedTrip = sim.state.passengerStates.collect {
        case (c, s) if AtStation(sim.state.passengers.find(_.code == c).get.destination) == s.currentPosition => s
      }.toList
    )
