package controller.simulation

import controller.ScreenTransition
import controller.statistics.StatisticsController
import utils.StageManager
import view.statistics.StatisticsView
import view.simconfig.{GraphView, RailView, StationView}

class StatisticsTransition(graphView: GraphView[StationView, RailView])
    extends ScreenTransition[StatisticsController, StatisticsView]:

  def build(): (StatisticsController, StatisticsView) =
    val controller = StatisticsController()
    val view = StatisticsView(graphView)
    (controller, view)

  override def afterAttach(controller: StatisticsController, view: StatisticsView): Unit =
    StageManager.getStage.title = "Simulation Statistics Report"
    controller.showStats()
