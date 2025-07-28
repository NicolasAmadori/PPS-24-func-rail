package model.mapgrid

sealed trait Cell

case object EmptyCell extends Cell

sealed trait RailPiece extends Cell
case object MetalRailPiece extends RailPiece
case object TitaniumRailPiece extends RailPiece

sealed trait StationPiece extends Cell
case object BigStationPiece extends StationPiece //Possibile suddivisione in parti diverse? centro, lato, angolo?
case object SmallStationPiece extends StationPiece
