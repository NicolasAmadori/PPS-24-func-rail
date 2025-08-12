package controller

import utils.StageManager
import view.View

trait Controller

trait BaseController[V] extends Controller:
  private var view: Option[V] = None

  def attachView(view: V): Unit =
    this.view = Some(view)

  def getView: V = view.getOrElse(
    throw new IllegalStateException("View has not been attached. Call attachView() first.")
  )

trait ScreenTransition[C <: BaseController[V], V <: View]:
  def build(): (C, V)

  def afterAttach(controller: C, view: V): Unit = ()

  def transition(): Unit =
    val (controller, view) = build()
    controller.attachView(view)
    StageManager.setRoot(view.getRoot)
    afterAttach(controller, view)
