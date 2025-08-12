package controller

import controller.simconfig.{HighSpeed, NormalSpeed, SimulationFormState}
import model.railway.EntityCodes.StationCode
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class SimulationFormStateTest extends AnyFlatSpec:

  "A SimulationFormState" should "start with an empty list of trains and zero index" in {
    val state = SimulationFormState()
    state.trains should be(List.empty)
    state.trainId should be(0)
  }

  it should "add a train and increment the trainId" in {
    val state = SimulationFormState()
    val (id, newState) = state.addTrain()

    newState.trains should have size 1
    newState.trains.head.id should be(id)
    newState.trainId should be(1)
  }

  it should "set a train to high speed" in {
    val state = SimulationFormState().addTrain()._2
    val newState = state.setHighSpeedTrain(0)

    newState.trains.head.trainType should be(HighSpeed)
  }

  it should "set a train to normal speed" in {
    val state = SimulationFormState().addTrain()._2.setHighSpeedTrain(0)
    val newState = state.setNormalSpeedTrain(0)

    newState.trains.head.trainType should be(NormalSpeed)
  }

  it should "set the departure station for a train" in {
    val state = SimulationFormState().addTrain()._2
    val newState = state.setDepartureStation(0, StationCode("StationA"))

    newState.trains.head.departureStation should be("StationA")
  }

  it should "add a stop to a train" in {
    val state = SimulationFormState().addTrain()._2
    val newState = state.addStop(0, StationCode("StationB"))

    newState.trains.head.stops should contain("StationB")
  }
