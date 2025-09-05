---
title:Amadori Nicolas
nav_order: 2
parent: Implementazione
---
# Amadori Nicolas

## MapGrid

La componente MapGrid rappresenta il cuore del modello, ovvero la griglia su cui vengono piazzati binari e stazioni.

È stata progettata seguendo lo stile funzionale di Scala: la classe MapGrid è una case class immutabile, e ogni operazione di piazzamento o rimozione di elementi restituisce una nuova istanza della griglia, senza effetti collaterali. In questo modo si mantiene la tracciabilità degli stati successivi e si semplificano eventuali analisi.

### Gerarchia dei tipi

La modellazione delle celle è realizzata con `sealed trait`, così che il compilatore possa garantire l’**esaustività del pattern matching**.

```scala
sealed trait CellType
case object EmptyType extends CellType

sealed trait RailType extends CellType
case object MetalRailType extends RailType
case object TitaniumRailType extends RailType

sealed trait StationType extends CellType
case object BigStationType extends StationType
case object SmallStationType extends StationType
```

A partire da questi tipi, sono definiti i vari `Cell`, `RailPiece` e `StationPiece`

### Piazzamento e vincoli

L’operazione principale è place, che restituisce un `Either[PlacementError, MapGrid]`.
Questo pattern evita eccezioni e fornisce una gestione esplicita e funzionale degli errori:

```scala
def place(x: Int, y: Int, cellType: CellType): Either[PlacementError, MapGrid] =
  if isWithinBudget(cellType) || cellType == EmptyType then
    cellType match
      case rail: RailType   => placeGenericRail(x, y, rail)
      case BigStationType   => placeBigStation(x, y)
      case SmallStationType => placeSmallStation(x, y)
      case EmptyType        => erasePiece(x, y)
  else
    Left(OutOfBudget())
```

```scala
def clearCells(coords: Seq[(Int, Int)]): MapGrid =
  copy(cells = coords.foldLeft(cells) {
    case (grid, (cx, cy)) if isInBounds(cx, cy) =>
      grid.updated(cy, grid(cy).updated(cx, EmptyCell))
    case (grid, _) => grid
  })
```

## RailwayMapper

Il modulo RailwayMapper si occupa della trasformazione di una rappresentazione grid-based (MapGrid) della mappa di gioco in un oggetto di dominio ad alto livello (Railway), che modella stazioni e binari come entità connesse.

L’idea è quella di separare il livello di rappresentazione della mappa (celle bidimensionali) dal livello logico (rete ferroviaria).

### Estrazione delle stazioni

Inizialmente si procede ad individuare tutte le stazioni: sia quelle piccole che quelle grandi composte da 9 celle.

```scala
val smallStations: List[(Station, Int, Int)] = extractSmallStations(mapGrid)
val bigStations: List[(Station, Int, Int)] = extractBigStations(mapGrid)
val expandedBigStations: List[(Station, Int, Int)] = 
	expandBigStations(mapGrid)(bigStations)

...

private def extractSmallStations(mapGrid: MapGrid): List[(Station, Int, Int)] =
  cellsWithCoords(mapGrid).collect {
    case ((x, y), SmallStationPiece(id)) =>
      (smallStation(s"$SMALL_STATION_PREFIX$id"), x, y)
  }.toList
  
/** Expands the representation of big stations to include their surrounding border cells. */
private def expandBigStations(mapGrid: MapGrid)(bigStations: List[(Station, Int, Int)]): List[(Station, Int, Int)] =
  bigStations.flatMap { (station, centerX, centerY) =>
    getSurroundingCells(mapGrid)(centerX, centerY).collect {
      case Some(_, borderX, borderY) => (station, borderX, borderY)
    }
  }
```

### Individuazione dei binari

Per ogni stazione individuata, si individuano binari collegati e si inizia la ricerca per definire le loro caratteristiche come tipologia, lunghezza e punto di arrivo.

A seconda della tipologia si impostano i metodi da utilizzare per la creazione degli oggetti binario.

```scala
stations.flatMap { (station, stationX, stationY) =>
      getCardinalCells(mapGrid)(stationX, stationY).collect {
        case Some((cell: MetalRailPiece, nx, ny)) =>
          (nx, ny, MetalRailType, metalRail, METAL_RAIL_PREFIX)
        case Some((cell: TitaniumRailPiece, nx, ny)) =>
          (nx, ny, TitaniumRailType, titaniumRail, TITANIUM_RAIL_PREFIX)
      }.flatMap { (nx, ny, railType, createRailFn, prefix) =>
        ...
        followRails(...)
    }
```

La funzione `followRails` è implementata come ricorsione di coda per seguire una sequenza di binari fino a trovare una stazione o un punto morto.
Inoltre viene gestito una sequenza di celle già visitate per evitare cicli infiniti:

```scala
@tailrec
private def followRails(mapGrid: MapGrid)(
    x: Int,
    y: Int,
    previousX: Int,
    previousY: Int,
    rail: Rail,
    railType: CellType,
    createRailFunction: (String, Int, String, String) => Rail,
    alreadyChecked: Set[(Int, Int)]
): Option[(Set[(Int, Int)], Rail)] = ...
```

## Viaggiatori

Per il calcolo degli itinerari dei passeggeri abbiamo implementato la classe `PassengerGenerator`, che permette di generare passeggeri con stazioni di partenza e destinazione randomizzate, e di associare loro un percorso definito secondo strategie diverse (casuale, più veloce, più economico o più breve).

### Calcolo degl itinerari

L’aspetto più interessante dell’implementazione è la funzione di ricerca degli itinerari, basata su una **DFS completamente funzionale**. Ogni chiamata esplora ricorsivamente le stazioni adiacenti, costruendo gli itinerari come liste immutabili di leg (`ItineraryLeg`) senza mutazioni intermedie:

```scala
/** Return all the possible itineraries between two stations */
private def findAllItineraries(
    start: StationCode,
    end: StationCode
): List[Itinerary] =
  dfs(current = start, target = end, visited = Set(start))

/** DFS that explore all possible paths */
private def dfs(
    current: StationCode,
    target: StationCode,
    visited: Set[StationCode],
    currentLegs: List[ItineraryLeg] = Nil
): List[Itinerary] =
  if current == target then
    List(Itinerary(currentLegs.reverse)) //Arrived at destination
  else
    trains
      .filter(_.stations.contains(current))
      .flatMap { train =>
        val idx = train.stations.indexOf(current)
        val neighbors = List(train.stations.lift(idx - 1), train.stations.lift(idx + 1)).flatten
        neighbors
          .filterNot(visited.contains)
          .flatMap { next =>
            val newLegs = currentLegs match
              case Nil => List(leg(train) from current to next)
              case head :: tail if head.train == train =>
                List(leg(train) from head.from to next) ::: tail
              case _ =>
                (leg(train) from current to next) :: currentLegs

            dfs(next, target, visited + next, newLegs)
          }
      }

```

Questo approccio sfrutta diversi aspetti del linguaggio quali la ricorsione con accumulatore immutabile: tutti gli stati intermedi (`currentLegs`, `visited`) sono immutabili, eliminando effetti collaterali e la composizione funzionale degli itinerari individuati tramite`flatMap`.

### Ottimizzazione degli itinerari

Una volta generate tutte le opzioni, l’ottimizzazione è semplice e funzionale, basata su `sortBy` e funzioni di ordine superiore. Ad esempio, per ottenere il percorso più veloce:

```scala
findAllItineraries(departure.code, arrival.code)
  .sortBy(_.totalTime)
  .headOption
```

In modo simile, si possono ottenere itinerari con distanza minima o costo minimo, senza mutazioni o logica imperativa.

## Controllo rete tramite Prolog

In questa parte ho implementato la verifica della connettività della rete ferroviaria sfruttando **tuProlog**. L’obiettivo era poter verificare la totale connessione della rete.

Ho creato un oggetto singleton `RailwayPrologChecker` per incapsulare l’engine Prolog che ha al suo interno un metodo principale `isRailwayConnected` che accetta un oggetto Railway e restituisce un booleano indicando se la rete è connessa o meno.

1. La rete viene convertita in fatti Prolog
    
    ```prolog
    station(ST1)
    station(ST2)
    rail(ST1, ST2)
    ```
    
2. I fatti vengono aggiunti alla teoria
3. Calcolo la soluzione del predicato `connected_graph.`

```prolog
%iterate between every pair of stations and check if they are all reachable
connected_graph :-
    findall(S, station(S), Stations),
    forall(member(X, Stations),
           forall(member(Y, Stations),
                  reachable(X, Y))).
```

## Simulazione

La classe `Simulation` rappresenta l’esecuzione della rete ferroviaria con treni, passeggeri e binari per una durata specificata, gestendo lo stato della simulazione passo passo. L’implementazione è funzionale e immutabile: ogni aggiornamento produce una nuova istanza di Simulation con lo stato modificato, evitando side-effect e semplificando il ragionamento sul comportamento del sistema.

Ogni passo di simulazione viene eseguito dal metodo `doStep`, che aggiorna stati dei treni, passeggeri e binari, genera nuovi passeggeri e registra i log relativi alle azioni compiute. La classe sfrutta case class immutabili e combinazioni di copie (copy) per modificare lo stato in modo controllato.

## DSL

Per semplificare la creazione di **passeggeri** e **itinerari**, ho implementato due **DSL fluide** (Domain-Specific Languages) in stile funzionale, che permettono di costruire oggetti complessi in modo leggibile e conciso.

Entrambe le DSL separano chiaramente la costruzione dei dati dalla logica di dominio e rendono la definizione di scenari di viaggio e test molto più leggibile

### Viaggiatori

Con `PassengerDSL` è possibile definire un passeggero specificando passo passo il codice, la stazione di partenza, la destinazione e opzionalmente un itinerario, sfruttando builder immutabili e metodi che ritornano nuovi oggetti con lo stato parziale già definito.

```scala
val p1 = PassengerDSL.passenger("P1").from("S1").to("S2").withNoItinerary
val itinerary = ...
val p2 = PassengerDSL.passenger("P2").from("S2").to("S3").withItinerary(itinerary)
```

### Itinerari

Analogamente, `ItineraryDSL` permette di creare tratte di un itinerario in modo naturale, utilizzando metodi infix come `from` e `to`, ottenendo una sintassi quasi parlata.

```scala
val itineraryLeg1 = leg(train) from stationA to stationB
val itineraryLeg2 = leg(train) from stationB to stationC
val itinerary = Itinerary(List(itineraryLeg1, itineraryLeg2))
```