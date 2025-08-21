package controller.simulation

import controller.BaseController
import controller.simulation.util.CsvWriter
import model.entities.PassengerPosition
import model.simulation.{Simulation, SimulationState}
import model.util.SimulationLog
import scalafx.application.Platform
import view.simulation.SimulationView

import java.awt.Desktop
import java.awt.Desktop.Action
import java.util.concurrent.{Executors, TimeUnit}

class SimulationController(simulation: Simulation) extends BaseController[SimulationView]:

  private var sim = simulation
  private val scheduler = Executors.newSingleThreadScheduledExecutor()

  private var stateUpdaterRaw: (List[String], List[String]) => Unit = (_, _) => ()
  private var eventListenerRaw: String => Unit = _ => ()
  private var progressIndicatorRaw: Double => Unit = _ => ()

  def attachStateUpdater(updater: (List[String], List[String]) => Unit): Unit =
    stateUpdaterRaw = updater

  def attachEventListener(listener: String => Unit): Unit =
    eventListenerRaw = listener

  def attachProgressIndicator(progressIndicator: Double => Unit): Unit =
    progressIndicatorRaw = progressIndicator

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

    Platform.runLater(() => stateUpdaterRaw(passengers, trains))

  private def emitEvent(message: String): Unit =
    Platform.runLater(() => eventListenerRaw(message))

  private def updateProgress(value: Double): Unit =
    Platform.runLater(() => progressIndicatorRaw(value))

  def startSimulation(): Unit =
    val (newSim, logs) = sim.start()
    sim = newSim
    logs.map(_.toString).foreach(emitEvent)
    updateState(sim.state)
    loopAsync(sim, 1000)

  private def loopAsync(current: Simulation, delayMs: Long): Unit =
    if current.isFinished then
      emitEvent(SimulationLog.SimulationFinished().toString)
      val stats = ReportGenerator.createReport(current)
      val file = CsvWriter.generateCsvFile(stats)
      if Desktop.isDesktopSupported then
        val desktop = Desktop.getDesktop
        if desktop.isSupported(Action.OPEN) then
          desktop.open(file)
    else
      scheduler.schedule(
        new Runnable:
          override def run(): Unit =
            current.doStep() match
              case Left(simError) => emitEvent(simError.toString)
              case Right((next, logs)) =>
                logs.map(_.toString).foreach(emitEvent)
                updateProgress(next.state.simulationStep.toDouble / (next.duration.toDouble * 24))
                updateState(next.state)
                loopAsync(next, delayMs)
        ,
        delayMs,
        TimeUnit.MILLISECONDS
      )
