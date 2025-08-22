package controller.simulation.util

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

object CsvWriter:
  def generateCsvFile(stats: List[Statistic], fileName: String = "simulation.csv"): File =
    val headings = "Metric;Value;Unit of measurement"
    val csv =
      (headings +: stats.map(s => List(s.toString, s.valueAsString, s.unit).mkString(";"))).mkString("\n")

    val path = Paths.get(fileName)
    Files.write(path, csv.getBytes(StandardCharsets.UTF_8))
    File(path.toUri)
