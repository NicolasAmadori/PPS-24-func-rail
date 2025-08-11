package model.simulation

import model.railway.Domain.StationCode
import model.railway.Rail.{metalRail, titaniumRail}
import model.railway.{MetalRail, Rail, Railway, TitaniumRail}

import scala.annotation.tailrec
import scala.collection.immutable.TreeSet

trait Graph:
  def rails(s: StationCode): List[Rail]

case class UndirectedGraph(adj: Map[StationCode, List[Rail]]) extends Graph:
  def rails(s: StationCode): List[Rail] = adj.getOrElse(s, Nil)

object Graph:
  def undirectedGraph(rails: List[Rail]): Graph =
    val reversedRails = rails.map {
      case r: MetalRail => metalRail(r.code.value, r.length, r.stationB.value, r.stationA.value)
      case r: TitaniumRail => titaniumRail(r.code.value, r.length, r.stationB.value, r.stationA.value)
    }
    UndirectedGraph((rails ++ reversedRails).groupBy(r => r.stationA).withDefaultValue(Nil))

object PathUtils:
  private val Infinity = Int.MaxValue

  case class StationNode(code: StationCode, distance: Int)
  given Ordering[StationNode] with
    def compare(a: StationNode, b: StationNode): Int =
      val distCompare = a.distance.compare(b.distance)
      if distCompare != 0 then distCompare
      else a.code.value.compareTo(b.code.value)

  def dijkstra(
      graph: Graph,
      start: StationCode,
      end: StationCode
  ): Option[List[Rail]] =
    @tailrec
    def dijkstraHelper(
        queue: TreeSet[StationNode],
        distances: Map[StationCode, Int],
        prev: Map[StationCode, (StationCode, Rail)]
    ): Option[List[Rail]] =
      if queue.isEmpty then None
      else
        val (currentNode, queueTail) = (queue.head, queue.tail)
        if currentNode.code == end then
          Some(reconstructPath(prev, end))
        else
          val (newQueue, newDistances, newPrev) = graph.rails(currentNode.code).foldLeft((queueTail, distances, prev)) {
            case ((q, d, p), r) =>
              val newDist = currentNode.distance + r.length
              if newDist < d.getOrElse(r.stationB, Infinity) then
                (
                  q + StationNode(r.stationB, newDist),
                  d.updated(r.stationB, newDist),
                  p.updated(r.stationB, (currentNode.code, r))
                )
              else (q, d, p)
          }
          dijkstraHelper(newQueue, newDistances, newPrev)

    dijkstraHelper(
      TreeSet(StationNode(start, 0)),
      Map(start -> 0),
      Map.empty
    )

  private def reconstructPath(prev: Map[StationCode, (StationCode, Rail)], target: StationCode): List[Rail] =
    @annotation.tailrec
    def loop(curr: StationCode, acc: List[Rail]): List[Rail] =
      prev.get(curr) match
        case Some((p, rail)) =>
          loop(p, rail :: acc)
        case None => acc

    loop(target, Nil)

object TSPHeuristics:
  private type PathInfo = List[Rail]
  private type ShortestPaths = Map[(StationCode, StationCode), PathInfo]
  def greedyTSP(
      start: StationCode,
      stops: Set[StationCode],
      shortestPaths: ShortestPaths
  ): List[Rail] =
    @tailrec
    def helper(current: StationCode, unvisited: Set[StationCode], route: List[StationCode]): List[StationCode] =
      if unvisited.isEmpty then
        route
      else
        val (next, _) = unvisited
          .flatMap { stop =>
            shortestPaths.get((current, stop)).map(rails =>
              stop -> rails.map(_.length).sum
            )
          }
          .minBy(_._2)
        helper(next, unvisited - next, route :+ next)

    def expandRoute(
        stationRoute: List[StationCode],
        shortestPaths: ShortestPaths
    ): List[Rail] =
      stationRoute
        .sliding(2)
        .flatMap {
          case List(a, b) =>
            shortestPaths((a, b))
        }
        .toList

    val route = helper(start, stops - start, List(start))
    expandRoute(route, shortestPaths)

trait RouteStrategy[T <: Train]:
  def computeRoute(railway: Railway, departure: StationCode, stops: List[StationCode]): Option[List[Rail]]

object RouteStrategy:

  import PathUtils.*
  import TSPHeuristics.*
  import Graph.*

  given RouteStrategy[NormalTrain] with
    def computeRoute(railway: Railway, departure: StationCode, stops: List[StationCode]): Option[List[Rail]] =
      val rails = railway.rails.collect { case c: MetalRail => c }
      computeRouteFromRails(departure, stops, rails)

  given RouteStrategy[HighSpeedTrain] with
    def computeRoute(railway: Railway, departure: StationCode, stops: List[StationCode]): Option[List[Rail]] =
      computeRouteFromRails(departure, stops, railway.rails)

  private def computeRouteFromRails(
      departure: StationCode,
      stops: List[StationCode],
      rails: List[Rail]
  ): Option[List[Rail]] =
    val graph = undirectedGraph(rails)
    val relevantNodes = departure +: stops
    val metaGraph = (for
      src <- relevantNodes
      dest <- relevantNodes
      if src != dest
    yield
      val result = dijkstra(graph, src, dest)
      (src, dest) -> result.getOrElse(Nil)
    ).toMap
    val route = greedyTSP(departure, stops.toSet, metaGraph)
    val routeStations = route.flatMap(r => List(r.stationA, r.stationB)).distinct
    if relevantNodes.forall(n => routeStations.contains(n)) then
      Some(route)
    else
      None

object RouteHelper:
  def getRouteForTrain[T <: Train](train: T, railway: Railway, dep: StationCode, stops: List[StationCode])(using
      strategy: RouteStrategy[T]
  ): Option[List[Rail]] =
    strategy.computeRoute(railway, dep, stops)
