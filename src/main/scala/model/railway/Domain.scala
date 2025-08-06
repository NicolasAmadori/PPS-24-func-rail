package model.railway

object Domain:
  opaque type StationCode = String
  object StationCode:
    def apply(code: String): StationCode = code
    @deprecated
    def fromString(code: String): StationCode = code
    def value(code: StationCode): String = code
    def listOf(code1: String, codes: String*): List[StationCode] =
      (code1 :: codes.toList).map(StationCode(_))
    def empty: StationCode = StationCode("")

  opaque type RailCode = Int
  object RailCode:
    def apply(code: Int): RailCode = code
    @deprecated
    def fromInt(code: Int): RailCode = code
    def value(code: RailCode): Int = code

  opaque type TrainCode = String
  object TrainCode:
    def apply(code: String): TrainCode = code
    def fromString(code: String): TrainCode = code

