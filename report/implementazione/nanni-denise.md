---
title: Nanni Denise
nav_order: 1
parent: Implementazione
---
# Denise Nanni

## Treni

Per la modellazione del treno, si sono mantenuti separati i concetti statici relativi alle informazioni non mutabili (velocità e tratta) e quelli dinamici, collegati allo stato del treno `TrainState`, come posizione e progresso. Lo stato progredisce seguendo le informazioni della `Route` e al momento dell’aggiornamento, il treno può trovarsi in tre situazioni:

- si trova su un binario e non ha ancora finito di attraversarlo, viene incrementato il progresso di uno step
- si trova su un binario e ha terminato il percorso, deve modificare la sua posizione per entrare nella stazione di arrivo, `stationB` se il treno sta progredendo in direzione `forward` o `stationA` se sta tornando indietro
- è in una stazione e deve proseguire su un binario, deve ottenere dalla route il binario successivo, controllare sia occupato o guasto e occuparlo
    - se è occupato, rimane nella stazione finché non si libera
    - se è guasto, prova a scegliere un altro binario o attende finché non è riparato
    - può transitarvi, deve aggiornare la sua posizione e il `travelTime` con il tempo che impiega il treno a percorrere quel tipo di `Rail`

Una volta che il treno ha raggiunto la fine della tratta, esso ricomincia a percorrerla nella direzione inversa. Ogni aggiornamento genera anche un log per mostrare la progressione. Sulla base delle posizioni dei treni, il `SimulationState` si occupa di aggiornare coerentemente lo stato dei binari.

### Tratte

All’avvio della simulazione, ciascun treno viene creato con una tratta da percorrere, scelta cercando di minimizzare il tempo effettivo di viaggio. Il problema principale è l’obbligo di copertura delle stazioni selezionate come fermate, avvicinandosi molto al *problema del commesso viaggiatore (TSP)*, senza la necessità di tornare al punto di partenza. Per il calcolo delle tratte, quindi, è stato combinato Dijkstra con un algoritmo TSP greedy.

In base al tipo di treno cambiano alcuni parametri del calcolo, come ad esempio i binari da considerare e il tempo impiegato dal treno per percorrerli. Di conseguenza, anche il grafo risultante varia per tipo di treno. 

Per rendere facilmente estendibile il calcolo a nuovi tipi di treni e binari, si è fatto uso delle **type classes, given instance** e del **pattern strategy**.

In particolare, sono stati implementati dei given per la type class `WeightCalculator` per ciascun tipo di treno in modo da mappare correttamente il peso degli archi in base al tipo di treno e binario considerati.

```scala
/** Trait for implementing mapping of an edge weight */
trait WeightCalculator[E, T]:
  /** Provides a mapping of an edge based on the train. */
  def weight(edge: E, train: T): Double

/** Given instance to provide weight mapping for NormalTrain */
given WeightCalculator[Rail, NormalTrain] with
  def weight(edge: Rail, train: NormalTrain): Double = edge.length / train.speed

/** Given instance to provide weight mapping for HighSpeedTrain */
given WeightCalculator[Rail, HighSpeedTrain] with
	// Depending on the type of the rail, the train should proceed at different speeds
  def weight(edge: Rail, train: HighSpeedTrain): Double = edge match
    case _: MetalRail => edge.length / Train.defaultSpeed
    case _: TitaniumRail => edge.length / train.speed
```

Il pattern strategy, insieme alle given instance, è stato utilizzato per iniettare la strategia di calcolo della route, in modo da costruire grafi diversi in base alle limitazioni di compatibilità tra binari e treni. La `RouteStrategy` è stata definita con tipo parametrico vincolato, in modo che possa essere usata solo per espandere le funzionalità dei treni.

```scala
trait RouteStrategy[T <: Train]:
  def computeRoute(railway: Railway, departure: StationCode, stops: List[StationCode], train: T): Option[Route]

/** Normal trains should consider only metal rails */
given normalTrainStrategy: RouteStrategy[NormalTrain] with
  def computeRoute(
      railway: Railway,
      departure: StationCode,
      stops: List[StationCode],
      train: NormalTrain
  ): Option[Route] =
    val rails = railway.rails.collect { case c: MetalRail => c }
    computeRouteFromRails(departure, stops.toSet, rails, train)

/** In this case, an high speed train have no constraints, but extensions are made easy */
given highSpeedStrategy: RouteStrategy[HighSpeedTrain] with
  def computeRoute(
      railway: Railway,
      departure: StationCode,
      stops: List[StationCode],
      train: HighSpeedTrain
  ): Option[Route] =
    computeRouteFromRails(departure, stops.toSet, railway.rails, train)
    
object RouteHelper:
  /** Computes the route for the given train.
    *
    * @param strategy
    *   the strategy to use for computing route
    */
  def getRouteForTrain[T <: Train, E <: Rail](train: T, railway: Railway)(using
      strategy: RouteStrategy[T]
  ): Option[Route] =
    strategy.computeRoute(railway, train.departureStation, train.stations, train)

```

Per gli algoritmi di Dijkstra e TSP si è mantenuto un approccio puramente funzionale, utilizzando la ricorsione per evitare stati mutabili. Per ogni treno, vengono calcolati i cammini minimi dalla stazione di partenza a tutte le fermate, poi viene costruito un meta grafo con solo le stazioni rilevanti e viene calcolato TSP su di esso. 

Per utilizzare la funzione `getRouteForTrain` è necessario il *pattern matching* sul tipo concreto di treno perché, in presenza di più given applicabili, il compilatore non può determinare automaticamente quale istanza usare per il tipo astratto `Train`. Il pattern matching guida la risoluzione delle type class, eliminando l’ambiguità.

## DSL

Per rendere più agevole la creazione di treni, in particolare per ovviare alla limitazione del calcolo della route che richiede di avere un treno già istanziato, è stato creato un DSL `TrainBuilder`, aderente al pattern builder, che si occupa di incapsulare l’assegnazione della route. 

Le informazioni che sono richieste per la creazione del treno sono: 

- il nome
- il tipo
- la stazione di partenza
- l’elenco delle fermate
- opzionalmente, la `Railway`

Tutti i metodi sono dichiarati infissi, perciò la creazione del treno può essere fatta:

```scala
train(c.name):
  _ ofType Normal in railway departsFrom c.departureStation stopsAt c.stops
```

```scala
def build: Train =
  val dep = departure.getOrElse(throw IllegalStateException("Train departure is required"))
  val st =
    Some(
      stops.filterNot(_ == dep)
    ).filterNot(_.isEmpty).getOrElse(throw IllegalStateException("Train must have at least one stop"))
  val allStops = (dep +: st).distinct
  kind match
    case Some(Normal) =>
      val t = normalTrain(name, allStops)
      val route = railway.flatMap(r => RouteHelper.getRouteForTrain(t, r))
      route.map(r => t.withRoute(r)).getOrElse(t)
    case Some(HighSpeed) =>
      val t = highSpeedTrain(name, allStops)
      val route = railway.flatMap(r => RouteHelper.getRouteForTrain(t, r))
      route.map(r => t.withRoute(r)).getOrElse(t)
    case _ => throw IllegalStateException("Train type must be defined")
```

In questo modo si ha la possibilità di fare dei controlli in maniera più compatta e, se è stata definita una `Railway`, il treno creato avrà una `Route` inizializzata aderente alla struttura della rete ferroviaria.

## Statistiche

Al termine della simulazione, all’utente viene mostrata un’interfaccia riepilogativa con alcune statistiche che possono essere significative per analizzare le performance della rete. Il tipo di statistiche ottenute si riferisce principalmente ai tempi di attesa, il livello di utilizzo di treni e binari e la copertura della rete in termini di percentuale di passeggeri che riescono a completare il loro itinerario.

Per calcolare le statistiche è stato definito uno `StatisticProvider` che espone un metodo `compute`, implementato per ciascuna nuova statistica. Per aggiungere una statistica è sufficiente aggiungere un nuovo provider che ne effettui il calcolo. 

## View

Per facilitare le transizioni tra una view e l’altra, è stato implementato un meccanismo di transizione tra schermate nell’architettura MVC. È stato utilizzato un trait generico `ScreenTransition[C, V]`, parametrizzato sul tipo di controller e view di destinazione.

All’interno del trait sono incapsulate tre responsabilità principali:

- creazione dei componenti MVC con il metodo `build` che restituisce la coppia controller-view
- attacco e setup tramite `afterAttach`, che permette di eseguire del codice dopo che la view è stata attaccata al controller, utile ad esempio per l’inizializzazione del grafo
- esecuzione della transizione con `transition`, che si occupa di collegare controller e view, impostare il nuovo stage nell’applicazione e richiamare `afterAttach`

Per ciascuna transizione necessaria nell’applicazione, si sono implementate le classi concrete, in modo da poter passare agilmente i dati necessari alla visualizzazione successiva. 
