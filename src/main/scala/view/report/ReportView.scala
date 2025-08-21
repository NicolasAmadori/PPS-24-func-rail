package view.report

import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.layout.{BorderPane, HBox, Pane}
import scalafx.scene.control.{Alert, ListView}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.Alert.AlertType
import view.View
import view.simconfig.{GraphView, RailView, StationView}
import view.simulation.SimulationViewConstants.{DefaultWindowMinWidth, ElementHeight}

object SimulationViewConstants:
  val DefaultWindowMinWidth = 1300
  val DefaultWindowMinHeight = 800
  val OneThirdMinWidth: Int = DefaultWindowMinWidth / 3
  val DefaultPadding: Insets = Insets(10)
  val ElementHeight: Int = 760

class ReportView(graphView: GraphView[StationView, RailView]) extends View:
  private val logs = ObservableBuffer[String]()
  private val passengers = ObservableBuffer[String]()
  private val trains = ObservableBuffer[String]()

  private val logsListView = new ListView[String](logs):
    prefWidth = DefaultWindowMinWidth / 2
    prefHeight = ElementHeight

  private val hBox = new HBox(logsListView, graphView.getView(DefaultWindowMinWidth / 2)):
    prefWidth = DefaultWindowMinWidth
    prefHeight = ElementHeight

  private val alert = new Alert(AlertType.Error):
    title = "Error"

  def addLog(message: String): Unit =
    Platform.runLater:
      logs += message
      if !logsListView.focused.value then
        logsListView.scrollTo(logs.size - 1) // autoscroll

  def showError(title: String = "", error: String): Unit =
    Platform.runLater:
      alert.contentText = error.toString
      alert.headerText = title
      alert.showAndWait()
      Platform.exit()
      System.exit(0)

  private val root = new BorderPane:
    center = hBox
    padding = SimulationViewConstants.DefaultPadding

  def getRoot: Pane = root
