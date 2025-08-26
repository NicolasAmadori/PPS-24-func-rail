package model.entities.dsl

import model.entities.EntityCodes.StationCode
import model.entities.{ItineraryLeg, Train}

/** Domain-specific language (DSL) for building itinerary legs.
  */
object ItineraryDSL:

  /** Starts building an itinerary leg using the given train.
    *
    * @param train
    *   the train used for the leg
    * @return
    *   a builder to specify the departure station
    */
  def leg(train: Train): ItineraryLegBuilderFrom =
    ItineraryLegBuilderFrom(train)

  /** Builder to define the departure station of an itinerary leg.
    *
    * @param train
    *   the train used in the leg
    */
  final case class ItineraryLegBuilderFrom(train: Train):
    /** Specifies the departure station of the leg.
      *
      * @param station
      *   the departure station
      * @return
      *   a builder to specify the arrival station
      */
    infix def from(station: StationCode): ItineraryLegBuilderTo =
      ItineraryLegBuilderTo(train, station)

  /** Builder to define the arrival station of an itinerary leg.
    *
    * @param train
    *   the train used in the leg
    * @param from
    *   the already defined departure station
    */
  final case class ItineraryLegBuilderTo(train: Train, from: StationCode):
    /** Specifies the arrival station and builds the final itinerary leg.
      *
      * @param to
      *   the arrival station
      * @return
      *   the complete itinerary leg
      */
    infix def to(to: StationCode): ItineraryLeg =
      ItineraryLeg(train, from, to)
