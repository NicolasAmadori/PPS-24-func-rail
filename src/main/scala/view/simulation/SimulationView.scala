package view.simulation

import controller.simulation.SimulationController
import scalafx.geometry.Insets
import scalafx.scene.layout.{BorderPane, Pane}
import scalafx.scene.control.ListView
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
    prefHeight = 800

  def addLog(message: String): Unit =
    logs += message
    listView.scrollTo(logs.size - 1) // autoscroll

  private val root = new BorderPane:
    center = listView

  def getRoot: Pane = root

  controller.attachEventListener(addLog)
