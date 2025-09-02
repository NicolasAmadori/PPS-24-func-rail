package controller

import utils.StageManager
import view.View

/** Trait that enables the transition between two MVC.
  * @tparam C
  *   the target controller
  * @tparam V
  *   the target view
  */
trait ScreenTransition[C <: BaseController[V], V <: View]:
  /** Creates and returns the controller and the view */
  def build(): (C, V)

  /** Performs an action after the attachment of the view to the controller.
    * @param controller
    * @param view
    */
  def afterAttach(controller: C, view: V): Unit = ()

  /** Performs the transition */
  def transition(): Unit =
    val (controller, view) = build()
    controller.attachView(view)
    StageManager.setRoot(view.getRoot)
    afterAttach(controller, view)
