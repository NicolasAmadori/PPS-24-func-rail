package model.util

import model.entities.EntityCodes.StationCode

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
