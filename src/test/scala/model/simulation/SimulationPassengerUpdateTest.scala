package model.simulation

import controller.simconfig.{SimulationBuilder, SimulationFormState}
import model.entities.EntityCodes.StationCode
import model.entities.Rail.metalRail
import model.entities.{HighSpeedTrain, NormalTrain, Passenger, PassengerPosition, Station, Train}
import model.entities.Train.{highSpeedTrain, normalTrain}
import model.railway.Railway
import model.util.{Log, PassengerGenerator}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class SimulationPassengerUpdateTest extends AnyFlatSpec:

  val stationCode1 = "ST001"
  val stationCode2 = "ST002"
  val stationCode3 = "ST003"
  val rail1 = 1
  val rail2 = 2
  val stationCodes = List(stationCode1, stationCode2, stationCode3)

  val simulationDuration = 100

  val trainCode1 = "101"
  val trainCode2 = "202"
  val train1: NormalTrain = normalTrain(trainCode1, StationCode.listOf(stationCode1, stationCode2))
  val train2: HighSpeedTrain = highSpeedTrain(trainCode2, StationCode.listOf(stationCode2, stationCode3))

  val railway: Railway = Railway.withStations(
    List(
      Station.smallStation(stationCode1),
      Station.bigStation(stationCode2),
      Station.smallStation(stationCode3)
    )
  ).withRails(
    List(
      metalRail(rail1, 200, stationCode1, stationCode2),
      metalRail(rail2, 500, stationCode2, stationCode3)
    )
  )

  "Passengers" should "not be present initially" in {
    val state = SimulationState(List(train1, train2))

    state.passengers should be(empty)
  }

//  "Passengers" should "be generated and should be in a station" in {
//    val PLAYER_NUMBER = 5
//    val state = SimulationState(List(train1, train2))
//    val passengerGenerator = PassengerGenerator(railway, state.trains)
//    val (newState, newGenerator, logs) = state.generatePassengers(passengerGenerator)(PLAYER_NUMBER)
//
//    newState.passengers.size should be(PLAYER_NUMBER)
//    newState.passengerStates.size should be(PLAYER_NUMBER)
//    logs.size should be(PLAYER_NUMBER)
//
//    newState.passengerStates.foreach { (_, state) =>
//      assert(state.currentPosition match
//        case PassengerPosition.AtStation(station) => stationCodes.contains(station)
//        case PassengerPosition.OnTrain(_) => false)
//    }
//  }

//  "Passengers" should "get on a train if possible" in {
//    val train1WithRoute = train1.withRoute(RouteHelper.getRouteForTrain(train1, railway).get)
//    val train2WithRoute = train2.withRoute(RouteHelper.getRouteForTrain(train2, railway).get)
//
//    val state = SimulationState.empty.withTrains(List(train1WithRoute, train2WithRoute))
//    val passengerGenerator = PassengerGenerator(railway, state.trains)
//    val (newState, newGenerator, logs) = state.generatePassengers(passengerGenerator)(100)
//
//    val selectedPlayer = newState.passengers.find(p =>
//      p.itinerary.isDefined
//        && p.itinerary.get.start == StationCode(stationCode1)
//        && p.itinerary.get.end == StationCode(stationCode2)
//    )
//    if selectedPlayer.isDefined then
//      println(newState.trains)
//      println(newState.trainStates)
//      val (newState2, logs2) = newState.updateTrains()
//      println(newState.trainStates)
//      val (newState3, logs3) = newState2.updatePassengers()
//
//      val (newState4, logs4) = newState3.updateTrains()
//      println(newState.trainStates)
//      val (newState5, logs5) = newState4.updatePassengers()
//
//      println(newState5.passengers.find(p => p.code == selectedPlayer.get.code).get)
//      println(newState5.passengerStates.find((p, _) => p == selectedPlayer.get.code).get)
//
//      (logs ++ logs2 ++ logs3 ++ logs4 ++ logs5)
//        .filter(s =>
//          s.toString.contains(" " + selectedPlayer.get.code.toString + " ") ||
//          s.toString.contains("Train"))
//        .foreach(println)
//  }

  "Passengers" should "get on a train if possible" in {
    var localState = SimulationFormState()
    val (id1, newForm1) = localState.addTrain()
//    val (id2, newForm2) = newForm1.addTrain()
    localState = newForm1

    localState = localState.setDepartureStation(id1, StationCode(stationCode1))
//    localState = localState.setDepartureStation(id2, StationCode(stationCode3))

    localState = localState.addStop(id1, StationCode(stationCode2))
//    localState = localState.addStop(id2, StationCode(stationCode2))

    localState = localState.updateTrainName(id1, "Primo")
//    localState = localState.updateTrainName(id2, "Secondo")

//    localState = localState.setHighSpeedTrain(id2)

    val result = SimulationBuilder.build(100, railway, localState.trains)

    result.isRight should be(true)

    var sim = result.toOption.get

    var logs: List[Log] = List.empty

    var selectedPlayer: Option[Passenger] = None
    while(selectedPlayer.isEmpty)
      val (s1, l) = sim.start()
      logs = l
      sim = s1
      selectedPlayer = sim.state.passengers.find(p =>
        p.itinerary.isDefined
          && p.itinerary.get.start == StationCode(stationCode2)
          && p.itinerary.get.end == StationCode(stationCode1)
      )

    val N_STEPS = 20

    selectedPlayer.get.itinerary.get.legs.foreach(l =>
      println(l)
      println(l.isForwardRoute)
    )
    (1 to N_STEPS + 1).foreach(n =>
      val res = sim.doStep()
      res.isRight should be(true)
      val (s, l) = res.toOption.get
      sim = s
      logs = logs ++ l

      println(sim.state.trainStates)
    )

//    if selectedPlayer.isDefined then
//      println(newState.trains)
//      println(newState.trainStates)
//      val (newState2, logs2) = newState.updateTrains()
//      println(newState.trainStates)
//      val (newState3, logs3) = newState2.updatePassengers()
//
//      val (newState4, logs4) = newState3.updateTrains()
//      println(newState.trainStates)
//      val (newState5, logs5) = newState4.updatePassengers()
//
//      println(newState5.passengers.find(p => p.code == selectedPlayer.get.code).get)
//      println(newState5.passengerStates.find((p, _) => p == selectedPlayer.get.code).get)

    println("\n\nlogs:")
    logs.foreach(println)
//    (logs1 ++ logs2 ++ logs3 ++ logs4 ++ logs5)
//        .filter(s =>
//          s.toString.contains(" " + selectedPlayer.get.code.toString + " ") ||
//            s.toString.contains("Train")
//        )
//      .foreach(println)

  }
