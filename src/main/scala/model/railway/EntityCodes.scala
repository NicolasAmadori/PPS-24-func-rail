package model.railway

object EntityCodes:
  opaque type StationCode = String
  object StationCode:
    def apply(code: String): StationCode = code
    def value(code: StationCode): String = code
    def listOf(code1: String, codes: String*): List[StationCode] =
      (code1 :: codes.toList).map(StationCode(_))
    def empty: StationCode = StationCode("")

  extension (code: StationCode)
    def value: String = code

  opaque type RailCode = Int
  object RailCode:
    def apply(code: Int): RailCode = code
    def value(code: RailCode): Int = code

  extension (code: RailCode)
    def value: Int = code

  opaque type TrainCode = String
  object TrainCode:
    def apply(code: String): TrainCode = code
    def empty: TrainCode = TrainCode("")

    extension (code: TrainCode)
      def isEmpty: Boolean = code == empty
      def value: String = code
