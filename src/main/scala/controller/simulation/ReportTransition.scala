package controller.simulation

import controller.ScreenTransition
import controller.report.ReportController
import utils.StageManager
import view.report.ReportView
import view.simconfig.{GraphView, RailView, StationView}

class ReportTransition(graphView: GraphView[StationView, RailView])
    extends ScreenTransition[ReportController, ReportView]:

  def build(): (ReportController, ReportView) =
    val controller = ReportController()
    val view = ReportView(graphView)
    (controller, view)

  override def afterAttach(controller: ReportController, view: ReportView): Unit =
    StageManager.getStage.title = "Simulation Report"
