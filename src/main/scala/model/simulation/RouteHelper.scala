package model.simulation

import model.entities.EntityCodes.StationCode
import model.entities.{HighSpeedTrain, MetalRail, NormalTrain, Rail, Route, TitaniumRail, Train}
import model.entities.Rail.{metalRail, titaniumRail}
import model.railway.*
import model.util.TSPHeuristics.greedyTSP
import model.util.{EdgeLike, Graph, WeightCalculator}

given EdgeLike[Rail] with
  def from(e: Rail): StationCode = e.stationA
  def to(e: Rail): StationCode = e.stationB
  def reverse(e: Rail): Rail = e match
    case r: MetalRail => metalRail(r.code.value, r.length, r.stationB.value, r.stationA.value)
    case r: TitaniumRail => titaniumRail(r.code.value, r.length, r.stationB.value, r.stationA.value)

//** Given instance to provide weight mapping for NormalTrain */
given WeightCalculator[Rail, NormalTrain] with
  def weight(edge: Rail, train: NormalTrain): Double = edge.length / train.speed

/** Given instance to provide weight mapping for HighSpeedTrain */
given WeightCalculator[Rail, HighSpeedTrain] with
  def weight(edge: Rail, train: HighSpeedTrain): Double = edge match
    case _: MetalRail => edge.length / Train.defaultSpeed
    case _: TitaniumRail => edge.length / train.speed

/** Strategy to handle routes */
trait RouteStrategy[T <: Train]:
  def computeRoute(railway: Railway, departure: StationCode, stops: List[StationCode], train: T): Option[Route]

object RouteStrategy:

  import model.util.PathUtils.*

  /** Strategy to compute route for normal trains, involves selecting only the rails that the normal trains can cross */
  given normalTrainStrategy: RouteStrategy[NormalTrain] with
    def computeRoute(
        railway: Railway,
        departure: StationCode,
        stops: List[StationCode],
        train: NormalTrain
    ): Option[Route] =
      val rails = railway.rails.collect { case c: MetalRail => c }
      computeRouteFromRails(departure, stops.toSet, rails, train)

  given highSpeedStrategy: RouteStrategy[HighSpeedTrain] with
    def computeRoute(
        railway: Railway,
        departure: StationCode,
        stops: List[StationCode],
        train: HighSpeedTrain
    ): Option[Route] =
      computeRouteFromRails(departure, stops.toSet, railway.rails, train)

  private def computeRouteFromRails(
      departure: StationCode,
      stops: Set[StationCode],
      rails: List[Rail],
      train: Train
  ): Option[Route] =
    val graph = Graph.undirectedGraph(rails, summon[EdgeLike[Rail]])
    val metaGraph = (for
      src <- stops
      dest <- stops
      if src != dest
    yield
      // Pattern-matching required to instruct scala to use the correct given instance
      val result = train match
        case t: NormalTrain => dijkstra(graph, src, dest, t)
        case t: HighSpeedTrain => dijkstra(graph, src, dest, t)
      (src, dest) -> result.getOrElse(Nil)
    ).toMap
    // Pattern-matching required to instruct scala to use the correct given instance
    val railSeq = train match
      case t: NormalTrain => greedyTSP(departure, stops, metaGraph, t)
      case t: HighSpeedTrain => greedyTSP(departure, stops, metaGraph, t)
    val routeStations = railSeq.flatMap(r => List(r.stationA, r.stationB)).distinct
    if stops.forall(n => routeStations.contains(n)) then
      Some(Route(railSeq))
    else
      None

/** Helper object to compute routes */
object RouteHelper:
  /** Computes the route for the given train.
    *
    * @param train
    * @param railway
    *   the railway to derive the graph
    * @param strategy
    *   the strategy to use for computing route
    * @tparam T
    * @tparam E
    * @return
    *   the route of the train if present, None otherwise
    */
  def getRouteForTrain[T <: Train, E <: Rail](train: T, railway: Railway)(using
      strategy: RouteStrategy[T]
  ): Option[Route] =
    strategy.computeRoute(railway, train.departureStation, train.stations, train)
