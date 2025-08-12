package controller

import utils.StageManager
import view.View

trait ScreenTransition[C <: BaseController[V], V <: View]:
  def build(): (C, V)

  def afterAttach(controller: C, view: V): Unit = ()

  def transition(): Unit =
    val (controller, view) = build()
    controller.attachView(view)
    StageManager.setRoot(view.getRoot)
    afterAttach(controller, view)
