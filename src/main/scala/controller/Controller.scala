package controller

trait Controller

/** Train to represent a base controller with a view */
trait BaseController[V] extends Controller:
  private var view: Option[V] = None

  /** Attach the view to the controller */
  def attachView(view: V): Unit =
    this.view = Some(view)

  /** Retrieves the view or throws an exception if it is not attached */
  def getView: V = view.getOrElse(
    throw new IllegalStateException("View has not been attached. Call attachView() first.")
  )
