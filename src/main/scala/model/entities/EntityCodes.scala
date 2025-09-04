package model.entities

object EntityCodes:
  /** Type of the station codes */
  opaque type StationCode = String
  object StationCode:
    def apply(code: String): StationCode = code
    def listOf(code1: String, codes: String*): List[StationCode] =
      (code1 :: codes.toList).map(StationCode(_))
    def empty: StationCode = StationCode("")

    extension (code: StationCode)
      def value: String = code

  /** Type of the rail codes */
  opaque type RailCode = String
  object RailCode:
    def apply(code: String): RailCode = code
    def empty: RailCode = RailCode("")

    extension (code: RailCode)
      def value: String = code

  /** Type of the train codes */
  opaque type TrainCode = String
  object TrainCode:
    def apply(code: String): TrainCode = code
    def empty: TrainCode = TrainCode("")

    extension (code: TrainCode)
      def isEmpty: Boolean = code == empty
      def value: String = code

  /** Type of the passengers codes */
  opaque type PassengerCode = String
  object PassengerCode:
    def apply(code: String): PassengerCode = code

    def value(code: PassengerCode): String = code
