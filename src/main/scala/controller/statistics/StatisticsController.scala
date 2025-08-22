package controller.statistics

import controller.BaseController
import controller.simulation.util.Statistic
import view.statistics.StatisticsView

class StatisticsController(statistics: Seq[Statistic]) extends BaseController[StatisticsView]:

  def showStats(): Unit =
    getView.addStats(statistics)
