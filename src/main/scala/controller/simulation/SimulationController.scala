package controller.simulation

import controller.BaseController
import controller.simconfig.SimulationTransition
import model.entities.PassengerPosition
import model.simulation.{Simulation, SimulationState}
import model.util.SimulationLog
import scalafx.application.Platform
import view.simconfig.{GraphView, RailView, StationView}
import view.simulation.SimulationView

import java.util.concurrent.{Executors, TimeUnit}

class SimulationController(simulation: Simulation, graphView: GraphView[StationView, RailView]) extends BaseController[SimulationView]:

  private var sim = simulation
  private val scheduler = Executors.newSingleThreadScheduledExecutor()

  private def updateState(state: SimulationState): Unit =
    val passengers: List[String] = state.passengerStates
      .filterNot((pCode, pState) =>
        pState.currentPosition match
          case PassengerPosition.AtStation(s) => state.passengers
              .find(p => p.code == pCode).get.destination == s
          case _ => false
      )
      .map((pCode, pState) => "" + pCode + ": " + pState.currentPosition)
      .toList

    val trains: List[String] = state.trainStates
      .filter((_, tState) => tState.position.isDefined)
      .map((tCode, tState) => "" + tCode + ": " + tState.position.get)
      .toList

    getView.updateState(passengers, trains)

  def startSimulation(): Unit =
    val (newSim, logs) = sim.start()
    sim = newSim
    logs.map(_.toString).foreach(getView.addLog)
    updateState(sim.state)
    loopAsync(sim, 1000)

  private def loopAsync(current: Simulation, delayMs: Long): Unit =
    if current.isFinished then
      getView.addLog(SimulationLog.SimulationFinished().toString)
      Platform.runLater:
        val transition = new ReportTransition(graphView)
        transition.transition()
    else
      scheduler.schedule(
        new Runnable:
          override def run(): Unit =
            try
              current.doStep() match
                case Left(simError) =>
                  getView.showError("Simulation Error", simError.toString)

                case Right((next, logs)) =>
                  logs.map(_.toString).foreach(getView.addLog)
                  getView.setProgress(next.state.simulationStep.toDouble / (next.duration.toDouble * 24))
                  updateState(next.state)
                  loopAsync(next, delayMs)
            catch
              case ex: Throwable =>
                getView.showError("Simulation Error", ex.getMessage)
        ,
        delayMs,
        TimeUnit.MILLISECONDS
      )
