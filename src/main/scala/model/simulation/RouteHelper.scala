package model.simulation

import model.railway.Domain.StationCode
import model.railway.Rail.{metalRail, titaniumRail}
import model.simulation.TSPHeuristics.greedyTSP

import scala.annotation.tailrec
import scala.collection.immutable.TreeSet

/** Trait for data structures that can be used as edge */
trait EdgeLike[E]:
  /** The node from which the edge starts */
  def from(e: E): StationCode

  /** The node that the edge reaches */
  def to(e: E): StationCode

  /** @return the edge in reverse direction */
  def reverse(e: E): E

trait Graph[E]:
  def edges(s: StationCode): List[E]

/** Trait for implementing mapping of an edge weight */
trait WeightCalculator[E, T]:
  /** Provides a mapping of an edge based on the train.
    * @param edge
    * @param train
    * @return
    *   the new mapped weight
    */
  def weight(edge: E, train: T): Double

/** Represents a non-directed graph implemented with an adjacency list.
  * @param adj
  *   adjacency list
  * @param edgeLike
  *   the logic to get edges functionalities
  * @tparam E
  *   the type of the edge
  */
case class UndirectedGraph[E](adj: Map[StationCode, List[E]], edgeLike: EdgeLike[E]) extends Graph[E]:
  def edges(s: StationCode): List[E] = adj.getOrElse(s, Nil)

/** Companion object of Graph */
object Graph:
  /** Creates a non-directed graph.
    * @param edges
    *   the starting edges
    * @param edgeLike
    *   the logic to get edges functionalities
    * @tparam E
    *   the type of edge
    * @return
    *   the graph
    */
  def undirectedGraph[E](edges: List[E], edgeLike: EdgeLike[E]): Graph[E] =
    val reversed = edges.map(edgeLike.reverse)
    val combined = edges ++ reversed
    val grouped = combined.groupBy(edgeLike.from).withDefaultValue(Nil)
    UndirectedGraph(grouped, edgeLike)

object PathUtils:

  /** Class to map the weight to reach the station */
  case class StationNode(code: StationCode, weight: Double)

  /** Logic to provide ordering between StationNode
    */
  given Ordering[StationNode] with
    def compare(a: StationNode, b: StationNode): Int =
      val distCompare = a.weight.compare(b.weight)
      if distCompare != 0 then distCompare
      else a.code.value.compareTo(b.code.value)

  /** Dijkstra implementation to compute the shortest path from a source to a destination.
    * @param graph
    * @param start
    *   source node
    * @param end
    *   destination node
    * @param train
    *   train for which the computation is performed
    * @param weightCalc
    *   logic to map weight
    * @param edgeLike
    *   logic to get the edges functionalities
    * @tparam E
    *   type of the edge
    * @tparam T
    *   type of the train
    * @return
    *   the edges that build the shortest path, None if there is no path
    */
  def dijkstra[E, T](
      graph: Graph[E],
      start: StationCode,
      end: StationCode,
      train: T
  )(using
      weightCalc: WeightCalculator[E, T],
      edgeLike: EdgeLike[E]
  ): Option[List[E]] =

    val Infinity = Double.MaxValue

    @tailrec
    def helper(
        queue: TreeSet[StationNode],
        distances: Map[StationCode, Double],
        prev: Map[StationCode, (StationCode, E)]
    ): Option[List[E]] =
      if queue.isEmpty then None
      else
        val (current, queueTail) = (queue.head, queue.tail)
        if current.code == end then
          Some(reconstructPath(prev, end))
        else
          val (newQueue, newDistances, newPrev) = graph.edges(current.code).foldLeft((queueTail, distances, prev)) {
            case ((q, d, p), e) =>
              val neighbor = edgeLike.to(e)
              val newDist: Double = current.weight + weightCalc.weight(e, train)
              if newDist < d.getOrElse(neighbor, Infinity) then
                (
                  q + StationNode(neighbor, newDist),
                  d.updated(neighbor, newDist),
                  p.updated(neighbor, (current.code, e))
                )
              else (q, d, p)
          }
          helper(newQueue, newDistances, newPrev)

    def reconstructPath(prev: Map[StationCode, (StationCode, E)], target: StationCode): List[E] =
      @tailrec
      def loop(curr: StationCode, acc: List[E]): List[E] =
        prev.get(curr) match
          case Some((p, e)) => loop(p, e :: acc)
          case None => acc
      loop(target, Nil)

    helper(
      TreeSet(StationNode(start, 0)),
      Map(start -> 0),
      Map.empty
    )

/** Object to provide solution for the Travelling Salesman Problem */
object TSPHeuristics:
  /** Greedy implementation of the TSP that computes a path
    * @param start
    *   source node
    * @param stops
    *   the nodes that must be visited
    * @param shortestPaths
    *   map of all pairs of nodes with the path from one to the other
    * @param train
    * @param weightCalc
    * @tparam E
    * @tparam T
    * @return
    *   the shortest path to visit all the nodes
    */
  def greedyTSP[E, T](
      start: StationCode,
      stops: Set[StationCode],
      shortestPaths: Map[(StationCode, StationCode), List[E]],
      train: T
  )(using weightCalc: WeightCalculator[E, T]): List[E] =

    @tailrec
    def helper(current: StationCode, unvisited: Set[StationCode], route: List[StationCode]): List[StationCode] =
      if unvisited.isEmpty then route
      else
        val (next, _) = unvisited
          .flatMap { stop =>
            shortestPaths.get((current, stop)).map { path =>
              val cost = path.map(e => weightCalc.weight(e, train)).sum
              stop -> cost
            }
          }
          .minBy(_._2)
        helper(next, unvisited - next, route :+ next)

    def expandRoute(stationRoute: List[StationCode]): List[E] =
      stationRoute
        .sliding(2)
        .collect { case List(a, b) => shortestPaths((a, b)) }
        .flatten
        .toList

    val route = helper(start, stops - start, List(start))
    expandRoute(route)

import model.railway.*

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
trait RouteStrategy[T <: Train, E]:
  def computeRoute(railway: Railway, departure: StationCode, stops: List[StationCode], train: T): Option[List[E]]

object RouteStrategy:

  import PathUtils.*

  /** Strategy to compute route for normal trains, involves selecting only the rails that the normal trains can cross */
  given normalTrainStrategy: RouteStrategy[NormalTrain, Rail] with
    def computeRoute(
        railway: Railway,
        departure: StationCode,
        stops: List[StationCode],
        train: NormalTrain
    ): Option[List[Rail]] =
      val rails = railway.rails.collect { case c: MetalRail => c }
      computeRouteFromRails(departure, stops.toSet, rails, train)

  given highSpeedStrategy: RouteStrategy[HighSpeedTrain, Rail] with
    def computeRoute(
        railway: Railway,
        departure: StationCode,
        stops: List[StationCode],
        train: HighSpeedTrain
    ): Option[List[Rail]] =
      computeRouteFromRails(departure, stops.toSet, railway.rails, train)

  private def computeRouteFromRails(
      departure: StationCode,
      stops: Set[StationCode],
      rails: List[Rail],
      train: Train
  ): Option[List[Rail]] =
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
    val route = train match
      case t: NormalTrain => greedyTSP(departure, stops, metaGraph, t)
      case t: HighSpeedTrain => greedyTSP(departure, stops, metaGraph, t)
    val routeStations = route.flatMap(r => List(r.stationA, r.stationB)).distinct
    if stops.forall(n => routeStations.contains(n)) then
      Some(route)
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
      strategy: RouteStrategy[T, E]
  ): Option[List[Rail]] =
    strategy.computeRoute(railway, train.departureStation, train.stations, train)
