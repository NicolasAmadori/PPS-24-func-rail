package view.simulation

import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.layout.{BorderPane, HBox, Pane, VBox}
import scalafx.scene.control.{Alert, ListView, ProgressBar}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.Alert.AlertType
import view.View
import view.simconfig.{GraphView, RailView, StationView}
import view.simulation.SimulationViewConstants.{DefaultWindowMinWidth, ElementHeight, HalfElementHeight, OneThirdMinWidth}

object SimulationViewConstants:
  val DefaultWindowMinWidth = 1300
  val DefaultWindowMinHeight = 800
  val OneThirdMinWidth: Int = DefaultWindowMinWidth / 3
  val DefaultPadding: Insets = Insets(10)
  val ElementHeight: Int = 760
  val HalfElementHeight: Int = ElementHeight / 2

class SimulationView(graphView: GraphView[StationView, RailView]) extends View:
  private val logs = ObservableBuffer[String]()
  private val passengers = ObservableBuffer[String]()
  private val trains = ObservableBuffer[String]()

  private val logsListView = new ListView[String](logs):
    prefWidth = DefaultWindowMinWidth - OneThirdMinWidth
    prefHeight = HalfElementHeight

  private val trainsListView = new ListView[String](trains):
    prefWidth = OneThirdMinWidth
    prefHeight = HalfElementHeight

  private val passengersListView = new ListView[String](passengers):
    prefWidth = OneThirdMinWidth
    prefHeight = HalfElementHeight

  private val hBox1 = new HBox(10, passengersListView, trainsListView)

  private val vBox = new VBox(10, hBox1, logsListView)

  private val hBox = new HBox(vBox, graphView.getView(OneThirdMinWidth)):
    prefWidth = DefaultWindowMinWidth
    prefHeight = ElementHeight

  private val progressBar = new ProgressBar:
    prefWidth = DefaultWindowMinWidth
    progress = 0.0

  private val alert = new Alert(AlertType.Error):
    title = "Error"

  private def codeOf(item: String): String =
    item.takeWhile(_ != ':')

  private def updateBuffer(buffer: ObservableBuffer[String], newItems: List[String]): Unit =
    val newCodes = newItems.map(codeOf).toSet
    buffer.filterInPlace(item => newCodes.contains(codeOf(item)))

    newItems.foreach { item =>
      val code = codeOf(item)
      val idx = buffer.indexWhere(old => codeOf(old) == code)
      if idx == -1 then
        buffer += item
      else
        buffer.update(idx, item)
    }

  def updateState(p: List[String], t: List[String]): Unit =
    Platform.runLater:
      updateBuffer(passengers, p)
      updateBuffer(trains, t)

      if !passengersListView.focused.value then
        passengersListView.scrollTo(passengers.size - 1)

      if !trainsListView.focused.value then
        trainsListView.scrollTo(trains.size - 1)

  def addLog(message: String): Unit =
    Platform.runLater:
      logs += message
      if !logsListView.focused.value then
        logsListView.scrollTo(logs.size - 1) // autoscroll

  def setProgress(value: Double): Unit =
    Platform.runLater:
      progressBar.progress = Math.min(1.0, Math.max(0.0, value))

  def showError(title: String = "", error: String): Unit =
    Platform.runLater:
      alert.contentText = error.toString
      alert.headerText = title
      alert.showAndWait()
      Platform.exit()

  private val root = new BorderPane:
    center = hBox
    bottom = progressBar
    padding = SimulationViewConstants.DefaultPadding

  def getRoot: Pane = root
