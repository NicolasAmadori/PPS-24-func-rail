package model.mapgrid

//sealed trait Cell
//
//case object EmptyCell extends Cell
//
//sealed trait RailPiece extends Cell
//case object MetalRailPiece extends RailPiece
//case object TitaniumRailPiece extends RailPiece
//
//sealed trait StationType
//case object BigStation extends StationType //Possibile suddivisione in parti diverse? centro, lato, angolo?
//case object SmallStation extends StationType
//
//case class StationPiece(id: Int, stationType: StationType) extends Cell

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

case class BigStationPiece(id: Int) extends StationPiece:
  val cellType: CellType = BigStationType
case class SmallStationPiece(id: Int) extends StationPiece:
  val cellType: CellType = SmallStationType
