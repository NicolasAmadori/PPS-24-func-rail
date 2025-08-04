package utils

import scalafx.scene.Scene
import scalafx.stage.Stage

import scala.compiletime.uninitialized

object StageManager:
  private var primaryStage: Stage = uninitialized

  def init(stage: Stage): Unit =
    primaryStage = stage

  def setRoot(newRoot: scalafx.scene.Parent): Unit =
    primaryStage.scene = new Scene(newRoot)

  def getStage: Stage = primaryStage

  def onShown(action: () => Unit): Unit =
    if primaryStage != null then
      primaryStage.onShown = _ => action()
    else
      throw new IllegalStateException("Primary stage is not initialized.")
