package model.simulation

object Domain:
  opaque type PassengerCode = String

  object PassengerCode:
    def apply(code: String): PassengerCode = code

    def value(code: PassengerCode): String = code
