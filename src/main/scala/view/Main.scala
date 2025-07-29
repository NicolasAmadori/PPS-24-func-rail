package view

import scalafx.application.JFXApp3
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ScrollPane}
import scalafx.scene.layout.GridPane

object Main extends JFXApp3:

  override def start(): Unit =
    val gridSize = 50 // griglia 100x100
    val buttonSize = 15 // dimensione dei pulsanti

    val grid = new GridPane:
      hgap = 1
      vgap = 1
      padding = Insets(10)

    for row <- 0 until gridSize; col <- 0 until gridSize do
      val btn = new Button(s"")
      btn.minWidth = buttonSize
      btn.minHeight = buttonSize
      btn.maxWidth = buttonSize
      btn.maxHeight = buttonSize
      btn.onAction = _ =>
        btn.style = "-fx-background-color: #58D68D"
      grid.add(btn, col, row)

    val scrollPane = new ScrollPane:
      content = grid
      pannable = true

    stage = new JFXApp3.PrimaryStage:
      title = "ScalaFX Grid 100x100"
      scene = new Scene(800, 800):
        root = scrollPane
