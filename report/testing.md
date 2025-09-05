---
title: Testing
nav_order: 7
parent: Report
---
# Testing

I test sono concentrati principalmente sul model e su alcune funzionalità di utility, mentre per le parti di controller e di view abbiamo effettuato numerosi test manuali, includendo anche tester esterni per verificare il comportamento dell’applicazione anche in scenari inattesi.

# Tecnologie

Il testing dell’applicazione è stato effettuato con **ScalaTest**, utilizzando la versione **FlatSpec** che migliora la leggibilità complessiva. Il testing è stato portato avanti di pari passo con lo sviluppo del codice in modo da assicurarci di coprirlo in modo esteso, aggiungendo eventuali test di non regressione per verificare casi limite che si sarebbero potuti ripresentare.

La correttezza del codice è stata garantita ad ogni push tramite il sistema di **Continuous Integration** che abbiamo configurato grazie a GitHub Actions. Ad ogni push sulla repository, la suite di test viene rieseguita per intero, permettendo di avere una visione sempre chiara e definita del comportamento del codice e di rispondere prontamente a eventuali insuccessi.

## Coverage

Per verificare la copertura dei nostri test, abbiamo usato lo strumento **scoverage** che ci ha permesso di avere sempre una panoramica dettagliata di quali parti non fossero coperte dai test considerati.

Per i componenti del model abbiamo raggiunto una coverage media del 86%.

# Esempi rilevanti

## Controllo overlap delle stazione in MapGrid

```scala
  it should "not allow overlapping BigStationPieces" in {
    val grid = MapGrid.empty(10, 10)
      .place(4, 4, BigStationType).toOption.get

    val overlappingResult = grid.place(5, 5, BigStationType)
    overlappingResult should matchPattern {
      case Left(PlacementError.InvalidPlacement(_, _, _)) =>
    }
  }
```

## Test della tratta

```scala
"Route" can "be different for trains of different types" in {
  val railway = SampleRailway.railway2
  val stations = StationCode.listOf(StationA, StationB, StationC)
  val normal = normalTrain(trainCode, stations)
  val highSpeed = highSpeedTrain(trainCode, stations)
  val resultForNormal = RouteHelper.getRouteForTrain(
    normal,
    railway
  )
  val resultForHighSpeed = RouteHelper.getRouteForTrain(
    highSpeed,
    railway
  )
  resultForNormal should be(None)
  resultForHighSpeed match
    case Some(r) =>
      stations.foreach(s => r.stations should contain(s))
    case _ => fail("High speed trains should retrieve some result")
}
```

## Corretta generazione dei passeggeri

```scala
  "Passengers" should "be generated and should be in a station" in {
    val PLAYER_NUMBER = 5
    val state = SimulationState(List(train1, train2))
    val passengerGenerator = PassengerGenerator(railway, state.trains)
    val (newState, newGenerator, logs) = state.generatePassengers(passengerGenerator)(PLAYER_NUMBER)

    newState.passengers.size should be(PLAYER_NUMBER)
    newState.passengerStates.size should be(PLAYER_NUMBER)
    logs.size should be(PLAYER_NUMBER)

    newState.passengerStates.foreach { (_, state) =>
      assert(state.currentPosition match
        case PassengerPosition.AtStation(station) => stationCodes.contains(station)
        case PassengerPosition.OnTrain(_) => false)
    }
  }
```