import controller.simconfig.SimulationConfigController
import model.mapgrid.{EmptyCell, MapGrid, MetalRailPiece, SmallStationPiece, TitaniumRailPiece}
import model.util.RailwayMapper
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import utils.StageManager
import view.GraphUtil
import view.simconfig.SimulationConfigView

object SimConfigMain extends JFXApp3:

  val rows: Int = 50
  val cols: Int = 70

  override def start(): Unit =
    GraphUtil.createRailway()

    val cells = Vector(
      Vector(EmptyCell, EmptyCell, EmptyCell, EmptyCell, EmptyCell, EmptyCell, EmptyCell, EmptyCell),
      Vector(EmptyCell, EmptyCell, EmptyCell, EmptyCell, EmptyCell, EmptyCell, EmptyCell, EmptyCell),
      Vector(
        EmptyCell,
        SmallStationPiece(1),
        MetalRailPiece(),
        MetalRailPiece(),
        SmallStationPiece(2),
        TitaniumRailPiece(),
        SmallStationPiece(3),
        EmptyCell
      ),
      Vector(EmptyCell, EmptyCell, EmptyCell, EmptyCell, EmptyCell, EmptyCell, EmptyCell, EmptyCell),
      Vector(EmptyCell, EmptyCell, EmptyCell, EmptyCell, EmptyCell, EmptyCell, EmptyCell, EmptyCell)
    )
    val grid = MapGrid(8, 5, cells)

    val controller = SimulationConfigController(grid, RailwayMapper.convert(grid))
    val view = SimulationConfigView(controller)

    controller.attachView(view)

    val stageInstance = new JFXApp3.PrimaryStage:
      title = "func-rail map builder"
      minWidth = 1000
      minHeight = 800
      scene = new Scene(view.getRoot)
      onShown = _ => view.initGraph()

    StageManager.init(stageInstance)

    stage = stageInstance
