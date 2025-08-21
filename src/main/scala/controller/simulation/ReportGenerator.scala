package controller.simulation

import controller.simulation.util.*
import model.simulation.Simulation

object ReportGenerator:

  private val providers = List(
    MostUsedRailsProvider,
    AverageTrainWaitingProvider,
    MostUsedTrainsProvider,
    IncompleteTripsProvider,
    CompletedTripsProvider,
    StationsWithMostWaitingProvider,
    AverageTripDurationProvider,
    AveragePassengerWaitingProvider,
    AveragePassengerTravelTimeProvider
  )

  /** Creates a report of a simulation instance returning the list of statistics.
    *
    * @param simulation
    *   the simulation to analyze
    * @return
    *   the list of statistics
    */
  def createReport(simulation: Simulation): List[Statistic] =
    val context = SimulationContext.from(simulation)
    providers.map(_.compute(context))
