import controller.MapController
import model.mapgrid.MapGrid
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import view.MapView

object Main extends JFXApp3:

  val rows: Int = 50
  val cols: Int = 70

  override def start(): Unit =
    val model = MapGrid.empty(cols, rows)
    val controller = MapController(model)
    val view = MapView(cols, rows, controller)

    controller.attachView(view)

    stage = new JFXApp3.PrimaryStage:
      title = "func-rail map builder"
      scene = new Scene(view)
