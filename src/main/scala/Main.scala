import controller.mapbuilder.MapBuilderController
import model.mapgrid.MapGrid
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import utils.StageManager
import view.mapbuilder.MapBuilderView

object Main extends JFXApp3:

  val rows: Int = 50
  val cols: Int = 70

  override def start(): Unit =
    val model = MapGrid.empty(cols, rows)
    val controller = MapBuilderController(model)
    val view = MapBuilderView(cols, rows, controller)

    controller.attachView(view)

    val stageInstance = new JFXApp3.PrimaryStage:
      title = "Map Builder"
      scene = new Scene(view)

    StageManager.init(stageInstance)

    stage = stageInstance
