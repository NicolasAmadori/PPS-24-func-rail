package view.simulation

import controller.simulation.SimulationController
import scalafx.geometry.Insets
import scalafx.scene.layout.{BorderPane, HBox, Pane}
import scalafx.scene.control.{ListView, ProgressBar}
import scalafx.collections.ObservableBuffer
import view.View
import view.simconfig.{GraphView, RailView, StationView}
import view.simulation.SimulationViewConstants.{DefaultWindowMinWidth, OneThirdMinWidth}

object SimulationViewConstants:
  val DefaultWindowMinWidth = 1300
  val DefaultWindowMinHeight = 800
  val OneThirdMinWidth: Int = DefaultWindowMinWidth / 3
  val DefaultPadding: Insets = Insets(10)

class SimulationView(controller: SimulationController, graphView: GraphView[StationView, RailView]) extends View:
  private val logs = ObservableBuffer[String]()

  private val listView = new ListView[String](logs):
    prefWidth =  DefaultWindowMinWidth - OneThirdMinWidth
    prefHeight = 760

  private val hBox = new HBox(listView, graphView.getView(OneThirdMinWidth)):
    prefWidth = DefaultWindowMinWidth
    prefHeight = 760

  private val progressBar = new ProgressBar:
    prefWidth = DefaultWindowMinWidth
    progress = 0.0

  def addLog(message: String): Unit =
    logs += message
    listView.scrollTo(logs.size - 1) // autoscroll

  def setProgress(value: Double): Unit =
    progressBar.progress = Math.min(1.0, Math.max(0.0, value))

  private val root = new BorderPane:
    center = hBox
    bottom = progressBar
    padding = SimulationViewConstants.DefaultPadding

  def getRoot: Pane = root

  controller.attachEventListener(addLog)
  controller.attachProgressIndicator(setProgress)
  controller.startSimulation()
