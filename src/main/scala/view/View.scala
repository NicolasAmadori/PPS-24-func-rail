package view

import scalafx.scene.Parent

trait View:
  def getRoot: Parent
//  def showError(error: ErrorMessage, title: String = "Error"): Unit = {
//    // Default implementation can be overridden by specific views
//    println(s"$title: ${error}")
//  }
