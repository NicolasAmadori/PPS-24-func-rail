package view

import scalafx.scene.Parent

trait View:
  def getRoot: Parent
