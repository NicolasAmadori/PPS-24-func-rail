package controller.statistics

import controller.BaseController
import controller.simulation.util.{CsvWriter, Statistic}

import view.statistics.StatisticsView
import view.util.FileOpener

class StatisticsController(statistics: Seq[Statistic]) extends BaseController[StatisticsView]:

  /** Adds stats to the view */
  def showStats(): Unit =
    getView.addStats(statistics)

  /** Downloads the file and opens it */
  def downloadAndOpenFile(): Unit =
    val file = CsvWriter.generateCsvFile(statistics.toList)
    FileOpener.openFile(file)
