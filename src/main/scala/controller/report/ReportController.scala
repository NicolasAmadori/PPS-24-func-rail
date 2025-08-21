package controller.report

import controller.BaseController
import view.report.ReportView

class ReportController extends BaseController[ReportView]:

  def showStats(): Unit =
    val exampleStats = List(
      ("Passeggeri totali", "1245", "persone"),
      ("Treni in servizio", "8", "unità"),
      ("Media velocità", "45.3", "km/h")
    )

    getView.addStats(exampleStats)
