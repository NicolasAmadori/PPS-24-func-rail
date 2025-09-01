package controller.simulation

import controller.BaseController
import model.entities.PassengerPosition
import model.simulation.{Simulation, SimulationState}
import model.util.SimulationLog
import scalafx.application.Platform
import utils.CustomError
import view.simconfig.{GraphView, RailView, StationView}
import view.simulation.SimulationView

import java.util.concurrent.{Executors, TimeUnit}

class SimulationController(simulation: Simulation, speed: Int, graphView: GraphView[StationView, RailView])
    extends BaseController[SimulationView]:

  private var sim = simulation
  private val scheduler = Executors.newSingleThreadScheduledExecutor(r =>
    val t = new Thread(r)
    t.setDaemon(true)
    t
  )

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
    val stepFrequency: Long = (1.0 / (speed.toDouble / 60.0 / 1000.0)).toLong
    loopAsync(sim, stepFrequency)

  private def loopAsync(current: Simulation, delayMs: Long): Unit =
    if current.isFinished then
      getView.addLog(SimulationLog.SimulationFinished().toString)
      val statistics = ReportGenerator.createReport(current)
      Platform.runLater:
        val transition = new StatisticsTransition(getView.getLogs, statistics, graphView)
        transition.transition()
    else
      scheduler.schedule(
        new Runnable:
          override def run(): Unit =
            try
              current.doStep() match
                case Left(simError) =>
                  getView.showError(simError, "Simulation Error")
                case Right((next, logs)) =>
                  logs.map(_.toString).foreach(getView.addLog)
                  getView.setProgress(next.state.simulationStep.toDouble / (next.duration.toDouble * 24))
                  updateState(next.state)
                  loopAsync(next, delayMs)
            catch
              case ex: Throwable =>
                getView.showError(CustomError(ex.getMessage), "Simulation Error")
        ,
        delayMs,
        TimeUnit.MILLISECONDS
      )
