import controller.simconfig.SimulationConfigController
import model.simulation.Simulation
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import view.GraphUtil
import view.simconfig.SimulationConfigView

object SimConfigMain extends JFXApp3:

  val rows: Int = 50
  val cols: Int = 70

  override def start(): Unit =
    val model = Simulation.withRailway(GraphUtil.createRailway())
    val controller = SimulationConfigController(model)
    val view = SimulationConfigView(controller)

    controller.attachView(view)

    stage = new JFXApp3.PrimaryStage:
      title = "func-rail map builder"
      minWidth = 1000
      minHeight = 800
      scene = new Scene(view.getRoot)
      onShown = _ => view.initGraph()
