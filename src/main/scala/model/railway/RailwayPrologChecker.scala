package model.railway

import alice.tuprolog.*

object RailwayPrologChecker:

  private val engine = new Prolog()

  /** Check if a railway is connected.
    * @param railway
    *   the railway to check
    * @return
    *   True if connected, false otherwise
    */
  def isRailwayConnected(railway: Railway): Boolean =
    engine.setTheory(new Theory(getClass.getResource("/railwayTheory.pl").openStream()))
    val facts = toPrologFacts(railway)
    engine.addTheory(new Theory(facts))

    val isRailwayConnected = engine.solve("connected_graph.")
    isRailwayConnected.isSuccess

  /** Create stations and rails facts to add them to the theory.
    *
    * @param railway
    *   the railway to convert
    * @return
    *   the list of facts
    */
  private def toPrologFacts(railway: Railway): String =
    val stationsFacts = railway.stations.map(s => s"station(${s.code.toString.toLowerCase}).")
    val railsFacts = railway.rails.map { r =>
      val a = r.stationA.toString.toLowerCase
      val b = r.stationB.toString.toLowerCase
      s"rail($a, $b)."
    }
    (stationsFacts ++ railsFacts).mkString("\n")
