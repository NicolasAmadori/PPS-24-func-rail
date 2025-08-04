package utils

import scalafx.scene.Scene
import scalafx.stage.Stage

object StageManager:
  private var primaryStage: Option[Stage] = None

  def init(stage: Stage): Unit =
    primaryStage = Some(stage)

  def setRoot(newRoot: scalafx.scene.Parent): Unit =
    primaryStage.foreach(p => p.scene = new Scene(newRoot))

  def getStage: Stage = primaryStage.getOrElse(
    throw new IllegalStateException("StageManager has not been initialized. Call init() first.")
  )
