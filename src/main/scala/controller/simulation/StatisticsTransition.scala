package controller.simulation

import controller.ScreenTransition
import controller.simulation.util.Statistic
import controller.statistics.StatisticsController
import utils.StageManager
import view.statistics.StatisticsView
import view.simconfig.{GraphView, RailView, StationView}

class StatisticsTransition(logs: List[String], statistics: Seq[Statistic], graphView: GraphView[StationView, RailView])
    extends ScreenTransition[StatisticsController, StatisticsView]:

  def build(): (StatisticsController, StatisticsView) =
    val controller = StatisticsController(statistics)
    val view = StatisticsView(controller, logs, graphView)
    (controller, view)

  override def afterAttach(controller: StatisticsController, view: StatisticsView): Unit =
    StageManager.getStage.title = "Simulation Statistics Report"
    controller.showStats()
