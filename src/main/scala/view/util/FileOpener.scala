package view.util

import java.awt.Desktop
import java.awt.Desktop.Action
import java.io.File

object FileOpener:
  def openFile(file: File): Unit =
    if Desktop.isDesktopSupported then
      val desktop = Desktop.getDesktop
      if desktop.isSupported(Action.OPEN) then
        desktop.open(file)
