package model

object Domain:
  opaque type StationCode = String
  object StationCode:
    def fromString(code: String): StationCode = code
    def value(code: StationCode): String = code
    
  opaque type RailCode = Int
  object RailCode:
    def fromInt(code: Int): RailCode = code
    def value(code: RailCode): Int = code
    
  opaque type TrainCode = Int
  object TrainCode:
    def fromInt(code: Int): TrainCode = code
    def value(code: TrainCode): Int = code