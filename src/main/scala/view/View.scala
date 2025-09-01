package view

import scalafx.scene.Parent
import utils.ErrorMessage

trait View:
  def getRoot: Parent
  def showError(error: ErrorMessage, title: String = ""): Unit
