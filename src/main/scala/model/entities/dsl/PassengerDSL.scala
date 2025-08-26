package model.entities.dsl

import model.entities.EntityCodes.{PassengerCode, StationCode}
import model.entities.{Itinerary, Passenger, PassengerImpl}

/** Companion object for creating passengers using a fluent DSL.
  */
object PassengerDSL:

  /** Starts building a passenger from its code.
    *
    * @param code
    *   Passenger code as a string
    * @return
    *   Builder to specify the departure station
    */
  def passenger(code: String): PassengerBuilderFrom =
    PassengerBuilderFrom(PassengerCode(code))

  /** Builder to define the departure station */
  final case class PassengerBuilderFrom(code: PassengerCode):
    /** Specifies the departure station of the passenger.
      *
      * @param station
      *   Code of the departure station
      * @return
      *   Builder to define the destination
      */
    def from(station: StationCode): PassengerBuilderTo =
      PassengerBuilderTo(code, station)

  /** Builder to define the destination station */
  final case class PassengerBuilderTo(code: PassengerCode, departure: StationCode):
    /** Specifies the destination station of the passenger.
      *
      * @param dest
      *   Code of the destination station
      * @return
      *   Builder to define an itinerary or finalize the passenger
      */
    def to(dest: StationCode): PassengerBuilderItinerary =
      PassengerBuilderItinerary(code, departure, dest)

  /** Builder to specify the itinerary or finalize the passenger */
  final case class PassengerBuilderItinerary(
      code: PassengerCode,
      departure: StationCode,
      dest: StationCode
  ):
    /** Creates a passenger without an itinerary.
      *
      * @return
      *   Passenger with no itinerary
      */
    def withNoItinerary: Passenger =
      PassengerImpl(code, departure, dest, None)

    /** Creates a passenger with a specified itinerary.
      *
      * @param it
      *   Itinerary to assign to the passenger
      * @return
      *   Passenger with the given itinerary
      */
    def withItinerary(it: Itinerary): Passenger =
      PassengerImpl(code, departure, dest, Some(it))
