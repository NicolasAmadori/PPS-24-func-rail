package controller

trait Controller

trait BaseController[V] extends Controller:
  private var view: Option[V] = None

  def attachView(view: V): Unit =
    this.view = Some(view)

  def getView: V = view.getOrElse(
    throw new IllegalStateException("View has not been attached. Call attachView() first.")
  )
