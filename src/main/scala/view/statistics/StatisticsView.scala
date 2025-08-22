package view.statistics

import controller.simulation.util.{CsvWriter, Statistic}
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, ListView, TableColumn, TableView}
import scalafx.scene.layout.{BorderPane, HBox, Pane, VBox}
import scalafx.beans.property.StringProperty
import view.View
import view.simconfig.{GraphView, RailView, StationView}
import view.simulation.SimulationViewConstants.{DefaultPadding, DefaultWindowMinWidth, ElementHeight}
import view.statistics.SimulationViewConstants.HalfHeight
import view.util.FileOpener

object SimulationViewConstants:
  val DefaultWindowMinWidth = 1300
  val DefaultWindowMinHeight = 800
  val DefaultPadding: Insets = Insets(10)
  val ElementHeight: Int = 760
  val HalfHeight: Int = ElementHeight / 2

case class Stat(statistic: Statistic):
  val metricProperty: StringProperty = StringProperty(statistic.name)
  val valueProperty: StringProperty = StringProperty(statistic.valueAsString)
  val unitProperty: StringProperty = StringProperty(statistic.unit)

class StatisticsView(logs: List[String], graphView: GraphView[StationView, RailView]) extends View:
  private val stats = ObservableBuffer[Stat]()

  private val statsTable = new TableView[Stat](stats):
    columns ++= List(
      new TableColumn[Stat, String]("Metric"):
        cellValueFactory = _.value.metricProperty
        prefWidth = DefaultWindowMinWidth / 2 / 2
      ,
      new TableColumn[Stat, String]("Value"):
        cellValueFactory = _.value.valueProperty
        prefWidth = DefaultWindowMinWidth / 2 / 4
      ,
      new TableColumn[Stat, String]("Unit"):
        cellValueFactory = _.value.unitProperty
        prefWidth = DefaultWindowMinWidth / 2 / 4
    )
    prefHeight = HalfHeight

  private val logsList = new ListView[String](logs):
    prefHeight = HalfHeight

  private val hBox = new HBox(
    new VBox(statsTable, logsList):
      prefWidth = DefaultWindowMinWidth / 2
    ,
    graphView.getView(DefaultWindowMinWidth / 2)
  ):
    prefWidth = DefaultWindowMinWidth
    prefHeight = ElementHeight
    spacing = 5

  def addStats(data: Seq[Statistic]): Unit =
    Platform.runLater:
      stats.clear()
      stats ++= data.map(Stat(_))

  private val root = new BorderPane:
    center = hBox
    bottom = new Button("Download and open CSV"):
      onAction = _ =>
        val file = CsvWriter.generateCsvFile(stats.toList.map(_.statistic))
        FileOpener.openFile(file)
      margin = Insets(10, 0, 0, 0)
    padding = DefaultPadding

  def getRoot: Pane = root
