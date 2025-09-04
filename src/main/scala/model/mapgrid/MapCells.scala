package model.mapgrid

/** Represents the type of cells in the grid.
  */
sealed trait CellType

/** Empty cell type (no infrastructure). */
case object EmptyType extends CellType

/** Base type for rail cell types. */
sealed trait RailType extends CellType
case object MetalRailType extends RailType
case object TitaniumRailType extends RailType

/** Base type for station cell types. */
sealed trait StationType extends CellType
case object BigStationType extends StationType
case object SmallStationType extends StationType

/** A cell in the grid, defined by its [[CellType]].
  */
sealed trait Cell:
  def cellType: CellType

case object EmptyCell extends Cell:
  val cellType: CellType = EmptyType

/** Base trait for rail pieces placed in the grid. */
sealed trait RailPiece extends Cell
case class MetalRailPiece() extends RailPiece:
  val cellType: CellType = MetalRailType
case class TitaniumRailPiece() extends RailPiece:
  val cellType: CellType = TitaniumRailType

/** Base trait for station pieces.
  */
sealed trait StationPiece extends Cell:
  def id: Int

/** Base for all pieces of a big station. */
sealed trait BigStationPiece extends StationPiece:
  val cellType: CellType = BigStationType

case class BigStationCenterPiece(id: Int) extends BigStationPiece
case class BigStationBorderPiece(id: Int) extends BigStationPiece

case class SmallStationPiece(id: Int) extends StationPiece:
  val cellType: CellType = SmallStationType
