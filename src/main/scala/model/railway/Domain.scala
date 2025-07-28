package model.railway

object Domain:
  opaque type StationCode = String
  object StationCode:
    def fromString(code: String): StationCode = code
    def value(code: StationCode): String = code
    def listOf(code1: String, codes: String*): List[StationCode] =
      (code1 :: codes.toList).map(fromString)

  opaque type RailCode = Int
  object RailCode:
    def fromInt(code: Int): RailCode = code
    def value(code: RailCode): Int = code

  opaque type TrainCode = Int
  object TrainCode:
    def fromInt(code: Int): TrainCode = code
    def value(code: TrainCode): Int = code
