---
title:Analisi dei requisiti
nav_order: 3
parent: Report
---
# Analisi dei requisiti

# Requisiti di business

Il committente richiede lo sviluppo di un applicativo di modellazione di reti ferroviarie e di simulazione e calcolo di efficienza delle stesse.

Le funzionalità principali richieste sono:

- interfaccia grafica minimale per la modellazione della rete ferroviaria con stazioni, binari e treni
- presenza di un budget opzionale per la costruzione della rete ferroviaria
- simulazione traffico ferroviario parametrizzato
- report statistico di fine simulazione

# Modello di dominio

Il dominio contiene tutti gli elementi che fanno parte delle reti ferroviarie odierne.

## Stazioni

Sono presenti vari tipologie di stazioni, differenziate dalla loro dimensione e affluenza: stazioni più grandi risulteranno avere una probabilità 5 volte superiore di essere stazione di partenza o di arrivo di un viaggiatore.

### Tipologie di stazioni

- Stazione piccola
    - dimensione 1x1
    - permette il collegamento fino a 4 binari diversi
- Stazione grande
    - dimensione 3x3
    - permette il collegamento fino a 9 binari diversi

### Regole di posizionamento

- Ogni stazione deve essere separata da qualsiasi altra stazione
- Ogni stazione deve essere collegata anche indirettamente a tutte le altre

## Binari

La rete ferroviaria prevede due tipi di binari. I binari in metallo supportano una velocità ridotta, mentre i binari in titanio permettono il transito a treni con velocità superiori. Ciascun binario può essere percorso in qualunque direzione e può ospitare al più un treno per volta.

### Tipologie di binari

- Binario di metallo
    - Velocità permessa: **100 km/h**
    - Costo di viaggio: **0,09 €/km**
- Binario di titanio
    - Velocità permessa: **300 km/h**
    - Costo di viaggio: **1,9 €/km**

### Regole di posizionamento

Un binario può essere posizionato solamente in uno di questi casi:

- Ha una qualsiasi stazione adiacente
- Ha un altro binario dello stesso tipo adiacente
- I binari non possono formare un quadrato 4x4 (risulterebbe in una progettazione ambigua)

## Treni

Una volta costruita la mappa, sarà possibile configurare un numero arbitrario di treni che dovranno muoversi su di essa. I treni si dividono in treni normali, caratterizzati da una velocità ridotta a `100 km/h`, che potranno transitare esclusivamente sui binari metallici e treni ad alta velocità che possono attraversare tutti i tipi di binari, con alcune limitazioni. Quando un treno ad alta velocità utilizza un binario in metallo, dovrà adattare la sua velocità di conseguenza, mettendosi al pari dei treni normali, mentre sulle tratte in titanio potrà sfruttare tutto il proprio potenziale avanzando a `300 km/h`. Per ciascun treno possono essere specificate la stazione di partenza e le stazioni in cui deve fermarsi.

Durante la simulazione, più treni potrebbero trovarsi a dover usare lo stesso binario. Dato che un binario può essere usato al più da un treno per volta, ciascun treno dovrà occuparsi di controllare se il binario desiderato è libero ed eventualmente occuparlo, altrimenti dovrà mettersi in attesa.

### Tratta

La scelta del percorso che il treno dovrà seguire dalla stazione di partenza includendo le altre verrà calcolato all’avvio della simulazione e rimarrà lo stesso, a meno di eventuali guasti che modificheranno temporaneamente la percorrenza. La tratta verrà scelta cercando di minimizzare la lunghezza del percorso e dovrà necessariamente includere tutte le stazioni definite come fermate, ma potrà all’occorrenza includere stazioni aggiuntive di transito, nel quale il treno non caricherà passeggeri. 

Durante la simulazione, il treno percorrerà la sua tratta in maniera continuativa, invertendo il senso di marcia una volta raggiunto il capolinea. Nell’eventualità in cui il binario prescelto fosse guasto, il treno ha due possibilità: scegliere un altro binario della rete ferroviaria per raggiungere la stazione successiva, oppure rimanere in attesa. La scelta di un binario alternativo è limitata dalla condizione che esso sia libero e che sia diretto, non sono ammessi cambi di tratte che modificano l’ordine di visita delle stazioni o che visitano stazioni non preventivate.

## Viaggiatori

I viaggiatori all'interno della simulazione saranno utenti con la necessità di raggiungere una determinata stazione di destinazione. In base al criterio scelto per la simulazione, l’itinerario potrà essere selezionato in modo casuale oppure ottimizzato rispetto a costo, tempo o distanza.

### Logica di generazione

- All’inizio della simulazione viene generato un numero casuale di viaggiatori da 0 a 10
- Durante ogni iterazione della simulazione viene generato un numero casuale di viaggiatori da 0 a 3
- Ogni viaggiatore seleziona casualmente la stazione di partenza e di arrivo.
- Le stazioni grandi hanno 5 volte la probabilità di essere selezionate rispetto alle stazioni piccole per modellare più accuratamente la realtà.
- Ogni viaggiatore sceglie il proprio itinerario tramite il criterio impostato dall’utente durante la configurazione della simulazione
    - Random
    - Tempo inferiore
    - Distanza inferiore
    - Costo inferiore

## Guasti

Opzionalmente, durante la simulazione è possibile che si formino dei guasti che bloccano dei binari, sui quali i treni non potranno transitare per un certo periodo. Ciascun guasto avrà una durata limitata, al termine della quale si considererà risolto e la linea potrà ricominciare a circolare normalmente. 

I guasti incoraggeranno la creazione di una rete ferroviaria ridondante e con pochi punti di singola rottura.

### Logica di generazione

- I guasti vengono generati **con una determinata probabilità per tick di simulazione**.
    - La probabilità è impostata a **0.05 (5%)**.
    - Questo significa che, in media, un guasto si verifica ogni 20 step di simulazione, se esistono binari candidabili.
- Un guasto può essere applicato solo a **binari liberi e non già guasti**.
    - Viene scelto casualmente un binario dalla lista dei candidati.
- La **durata del guasto** è determinata in modo casuale, fino a una durata massima.
    - La durata massima è impostata a **7 * 24 = 168 step** (una settimana simulata).
    - La distribuzione non è uniforme:
        - la probabilità che un guasto duri 1 step è maggiore rispetto a 2, e così via.
        - Data $n$ la durata del guasto in numero di step:
        
        $$
        P(n) = \frac{1}{n}
        $$
        
        - Quindi i guasti brevi sono molto più probabili dei guasti lunghi.

### Evoluzione del guasto

- Ad ogni tick, se un binario è in stato guasto, il suo **countdown** viene decrementato.
- Quando il countdown raggiunge 0, il binario torna operativo.
- Durante il periodo di guasto, i treni che devono percorrere quel binario:
    1. Tentano di deviare su un **binario alternativo diretto** per la stessa destinazione.
    2. Se non esiste un’alternativa, restano in attesa fino alla riparazione.

## Budget

Nella costruzione della rete ferroviaria, sarà possibile attivare una limitazione impostando un budget massimo entro il quale il costo complessivo della rete deve rientrare. Se l’opzione è abilitata, non dovrà essere possibile piazzare elementi sulla mappa che eccedano il budget. A ciascun elemento sarà associato un costo, stazioni più grandi saranno più dispendiose, così come binari più veloci, in particolare:

- stazioni grandi, costo `50`
- stazioni piccole, costo `30`
- binari in metallo, costo `1`
- binari in titanio, costo `3`

Se il budget viene attivato dopo aver già piazzato dei pezzi il cui costo complessivo è maggiore, si dovrà utilizzare lo strumento di rimozione per eliminare i pezzi in eccesso, altrimenti non sarà possibile procedere con la conversione della mappa.

## Simulazione

La simulazione procederà per step, ciascuno della durata logica di un’ora. In ciascuno step verranno aggiornati gli stati dei componenti come treni, binari e passeggeri per riflettere l’evoluzione. La simulazione potrà essere configurata per personalizzare l’esecuzione in base alle necessità. Si potrà:

- impostare una durata in giorni
- abilitare e disabilitare i guasti
- impostare il criterio di scelta degli itinerari scelti dai viaggiatori
- modificare la velocità di simulazione, intesa come il numero di step al minuto

### Messaggi di log

Durante la simulazione, ogni cambiamento di stato delle entità del dominio (stazioni, binari, viaggiatori) genera uno o più messaggio di log.
Questi messaggi permettono di monitorare l’evoluzione della simulazione e di verificare che le operazioni avvengano come previsto.

# Requisiti funzionali

## Utente

Utilizzando l'applicazione, l'utente deve essere in grado di:

- creare una rete ferroviaria
    - definire un budget massimo
    - scegliere l’elemento da posizionare
    - posizionare l’elemento selezionato sulla mappa
    - utilizzare il drag and drop per il posizionamento degli elementi
    - rimuovere gli elementi già posizionati sulla mappa
    - visualizzare la rete sotto forma di grafo
- navigare tra le interfacce
- configurare la simulazione
    - attivare o disattivare i guasti
    - definire la durata in giorni della simulazione
    - scegliere la velocità di avanzamento simulazione
    - impostare il criterio di scelta degli itinerari per i passeggeri
    - aggiungere ed eliminare treni
        - pianificare le tratte in termini di stazione di partenza e fermate
- monitorare la progressione della simulazione
    - visualizzare i messaggi di log prodotti durante la simulazione
    - visualizzare i viaggiatori e la loro posizione
    - visualizzare i treni e la loro posizione
    - visualizzare lo stato di avanzamento della simulazione
- controllare l'efficienza della rete modellata
    - ottenere un report con le statistiche a fine simulazione
    - generare e scaricare un file con le statistiche

## Sistema

I requisiti del sistema sono:

- modellazione del tempo per progressione della simulazione e per la stima dei tempi di percorrenza
- conversione della rete ferroviaria progettata in una struttura dati orientata al calcolo
- aggiunta dei treni assegnandogli un nome, un tipo, una stazione di partenza e le fermate
    - calcolo del percorso ottimale da seguire per coprire tutte le fermate
- gestire eventuali sovrapposizioni di tratte tra i treni
- controllo della correttezza della rete ferroviaria progettata
- calcolo degli itinerari possibili tramite algoritmi di ricerca
- generazione di passeggeri e guasti in maniera casuale durante la simulazione
- calcolo delle statistiche di viaggio sulla base degli spostamenti di treni e passeggeri

# Requisiti non funzionali

- **Usabilità**: l’interfaccia grafica deve essere minimale e intuitiva, richiedendo un numero limitato di click per completare operazioni comuni come creare stazioni, collegarle, avviare la simulazione.
- **Portabilità**: il sistema deve essere eseguibile su tutti i sistemi operativi
- **Affidabilità**: il sistema deve gestire correttamente errori di input e condizioni limite, ad esempio cicli infiniti nella rete ferroviaria, senza causare crash.
- **Trasparenza simulativa**: durante la simulazione, il sistema deve fornire una visualizzazione testuale chiara che permetta all'utente di comprendere facilmente lo stato della simulazione.

# Requisiti di implementazione

L'implementazione del sistema deve utilizzare:

- Scala 3
- Prolog
- JDK 21+
- SBT
- ScalaFX