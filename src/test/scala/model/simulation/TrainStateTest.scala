package model.simulation

import model.entities.EntityCodes.StationCode
import model.entities.MetalRail
import model.entities.dsl.buildNormalTrain
import model.simulation.TrainPosition.{AtStation, OnRail}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*
import util.SampleRailway
import util.SampleRailway.SampleStation.{StationA, StationB}

class TrainStateTest extends AnyFlatSpec:
  private val railway = SampleRailway.railway6
  private val train = buildNormalTrain("T1"):
    _ in railway departsFrom StationCode(StationA) stopsAt StationCode(StationB)
  private val firstRail = train.route.getRailAt(0).code
  private val railStates = railway.rails.map(r => r.code -> RailState(r.code)).toMap
  private val trainState = TrainState(AtStation(railway.stationCodes.head))

  "Train state" should "choose another rail if the selected one is faulty" in {
    val brokenRail = railStates.updatedWith(firstRail)(v => v.map(_.setFaulty(2)))

    val newState = trainState.update(train, railway.rails, brokenRail)
    newState._1.travelTime should be(train.getTravelTime(railway.rails(1)))
    newState._2 should be(OnRail(railway.rails(1).code))
  }

  it should "not update if there are no free alternative rails" in {
    val updatedRailStates = railStates.map { (c, r) =>
      if c == firstRail then
        c -> r.setFaulty(2)
      else
        c -> r.setOccupied()
    }.toMap

    val newState = trainState.update(train, railway.rails, updatedRailStates)
    newState._2 should be(AtStation(train.route.startStation.get))
  }

  it should "update if rail is repaired" in {
    val updatedRailStates = railStates.map { (c, r) =>
      if c == firstRail then
        c -> r.setFaulty(2)
      else
        c -> r.setOccupied()
    }.toMap

    val stateWithBrokenRail = trainState.update(train, railway.rails, updatedRailStates)
    stateWithBrokenRail._2 should be(AtStation(train.route.startStation.get))

    val nextState = stateWithBrokenRail._1.update(train, railway.rails, railStates)
    nextState._2 should be(OnRail(railway.rails.head.code))
  }

  it should "not update if there is no alternative rail for its train type" in {
    val statesWithOnlyTitaniumFree = railStates.map { (c, r) =>
      if c == firstRail then
        c -> r.setFaulty(2)
      else if railway.rails.filter(_.code == c).head.isInstanceOf[MetalRail] then
        c -> r.setOccupied()
      else
        c -> r
    }.toMap

    val stateWithBrokenRail = trainState.update(train, railway.rails, statesWithOnlyTitaniumFree)
    stateWithBrokenRail._2 should be(AtStation(train.route.startStation.get))
  }
