package controller.simconfig

import controller.ScreenTransition
import controller.mapbuilder.MapBuilderController
import model.mapgrid.MapGrid
import utils.StageManager
import view.mapbuilder.MapBuilderView

class MapBuilderTransition(model: MapGrid)
    extends ScreenTransition[MapBuilderController, MapBuilderView]:

  def build(): (MapBuilderController, MapBuilderView) =
    val controller = MapBuilderController(model)
    val view = MapBuilderView(model.width, model.height, controller)
    (controller, view)

  override def afterAttach(controller: MapBuilderController, view: MapBuilderView): Unit =
    StageManager.getStage.title = "Map Builder"
