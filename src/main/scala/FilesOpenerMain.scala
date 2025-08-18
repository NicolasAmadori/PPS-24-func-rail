import java.awt.Desktop
import java.awt.Desktop.Action
import java.io.File

object FilesOpenerMain:
  @main def run(args: String*): Unit =
    if Desktop.isDesktopSupported then
      val desktop = Desktop.getDesktop
      if desktop.isSupported(Action.OPEN) then
        val file = File("test.txt")
        desktop.open(file)