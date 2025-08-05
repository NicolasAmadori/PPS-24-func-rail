package model.mapgrid

sealed trait CellType

case object EmptyType extends CellType

sealed trait RailType extends CellType
case object MetalRailType extends RailType
case object TitaniumRailType extends RailType

sealed trait StationType extends CellType
case object BigStationType extends StationType
case object SmallStationType extends StationType

sealed trait Cell:
  def cellType: CellType

case object EmptyCell extends Cell:
  val cellType: CellType = EmptyType

sealed trait RailPiece extends Cell
case class MetalRailPiece() extends RailPiece:
  val cellType: CellType = MetalRailType
case class TitaniumRailPiece() extends RailPiece:
  val cellType: CellType = TitaniumRailType

sealed trait StationPiece extends Cell:
  def id: Int

sealed trait BigStationPiece extends StationPiece:
  val cellType: CellType = BigStationType

case class BigStationCenterPiece(id: Int) extends BigStationPiece
case class BigStationBorderPiece(id: Int) extends BigStationPiece

case class SmallStationPiece(id: Int) extends StationPiece:
  val cellType: CellType = SmallStationType
