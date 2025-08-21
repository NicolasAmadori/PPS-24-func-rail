package controller.statistics

import controller.BaseController
import view.statistics.StatisticsView

class StatisticsController extends BaseController[StatisticsView]:

  def showStats(): Unit =
    val exampleStats = List(
      ("Passeggeri totali", "1245", "persone"),
      ("Treni in servizio", "8", "unità"),
      ("Media velocità", "45.3", "km/h")
    )

    getView.addStats(exampleStats)
