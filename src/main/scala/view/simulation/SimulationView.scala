package view.simulation

import controller.simulation.SimulationController
import scalafx.geometry.Insets
import scalafx.scene.layout.{BorderPane, Pane}
import scalafx.scene.control.{ListView, ProgressBar}
import scalafx.collections.ObservableBuffer
import view.View

object SimulationViewConstants:
  val DefaultWindowMinWidth = 1300
  val DefaultWindowMinHeight = 800
  val DefaultPadding: Insets = Insets(10)

class SimulationView(controller: SimulationController) extends View:
  private val logs = ObservableBuffer[String]()

  private val listView = new ListView[String](logs):
    prefWidth = 1300
    prefHeight = 760

  private val progressBar = new ProgressBar:
    prefWidth = 1300
    progress = 0.0

  def addLog(message: String): Unit =
    logs += message
    listView.scrollTo(logs.size - 1) // autoscroll

  def setProgress(value: Double): Unit =
    progressBar.progress = Math.min(1.0, Math.max(0.0, value))

  private val root = new BorderPane:
    center = listView
    bottom = progressBar
    padding = SimulationViewConstants.DefaultPadding

  def getRoot: Pane = root

  controller.attachEventListener(addLog)
  controller.attachProgressIndicator(setProgress)
  controller.startSimulation()
