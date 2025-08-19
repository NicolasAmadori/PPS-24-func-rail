package controller.simulation

import controller.BaseController
import controller.simulation.util.CsvWriter
import model.simulation.Simulation
import scalafx.application.Platform
import view.simulation.SimulationView

import java.awt.Desktop
import java.awt.Desktop.Action

import java.util.concurrent.{Executors, TimeUnit}

class SimulationController(simulation: Simulation) extends BaseController[SimulationView]:

  private var sim = simulation
  private val scheduler = Executors.newSingleThreadScheduledExecutor()

  private var eventListenerRaw: String => Unit = _ => ()
  private var progressIndicatorRaw: Double => Unit = _ => ()

  def attachEventListener(listener: String => Unit): Unit =
    eventListenerRaw = listener

  def attachProgressIndicator(progressIndicator: Double => Unit): Unit =
    progressIndicatorRaw = progressIndicator

  private def emitEvent(message: String): Unit =
    Platform.runLater(() => eventListenerRaw(message))

  private def updateProgress(value: Double): Unit =
    Platform.runLater(() => progressIndicatorRaw(value))

  def startSimulation(): Unit =
    sim = sim.start()
    emitEvent("Simulation started")
    loopAsync(sim, 1000)

  private def loopAsync(current: Simulation, delayMs: Long): Unit =
    if current.isFinished then
      emitEvent("Simulation finished")
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
                loopAsync(next, delayMs)
        ,
        delayMs,
        TimeUnit.MILLISECONDS
      )
