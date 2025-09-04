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

/** Controller responsible for orchestrating a single simulation run and connecting the simulation model with the
  * graphical user interface.
  *
  * This class manages:
  *   - Executing the simulation in a background scheduled thread.
  *   - Forwarding simulation progress, logs, and statistics to the view.
  *   - Ensuring that UI updates are executed on the JavaFX application thread.
  *
  * @param simulation
  *   the initial [[Simulation]] instance to run.
  * @param speed
  *   the playback speed factor; higher values accelerate the simulation.
  * @param graphView
  *   reference to the railway graph, displayed in the statistics view.
  */
class SimulationController(simulation: Simulation, speed: Int, graphView: GraphView[StationView, RailView])
    extends BaseController[SimulationView]:

  private var sim = simulation

  private val scheduler = Executors.newSingleThreadScheduledExecutor(r =>
    val t = new Thread(r)
    t.setDaemon(true)
    t
  )

  /** Updates the UI with the latest simulation state.
    *
    * Passengers that already reached their destination are excluded from the view update. Only active passengers and
    * trains with a defined position are forwarded to the [[SimulationView]].
    *
    * @param state
    *   the current [[SimulationState]] snapshot.
    */
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

  /** Starts the simulation asynchronously.
    */
  def startSimulation(): Unit =
    val (newSim, logs) = sim.start()
    sim = newSim
    logs.map(_.toString).foreach(getView.addLog)
    updateState(sim.state)
    val stepFrequency: Long = (1.0 / (speed.toDouble / 60.0 / 1000.0)).toLong
    loopAsync(sim, stepFrequency)

  /** Executes the simulation asynchronously step by step.
    *
    * Each call either:
    *   - Concludes the simulation if finished, collecting statistics and transitioning to the report view, or
    *   - Performs the next simulation step, logs results, updates progress, and schedules the next iteration.
    *
    * Errors thrown inside the simulation are caught and displayed through the [[SimulationView]] error dialog.
    *
    * @param current
    *   the current simulation state.
    * @param delayMs
    *   the delay between steps in milliseconds, computed from playback speed.
    */
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
