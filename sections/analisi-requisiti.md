# Analisi dei requisiti

## Requisiti di business

Il committente richiede lo sviluppo di un applicativo di modellazione di reti ferroviarie e di simulazione e calcolo di efficienza delle stesse.

Le funzionalità principali richieste sono:

*   interfaccia grafica minimale per la modellazione della rete ferroviaria con stazioni, binari e treni;
*   simulazione traffico ferroviario parametrizzato;
*   report statistico di fine simulazione.

## Modello di dominio

Il dominio contiene tutti gli elementi che fanno parte delle reti ferroviarie odierne.

### Stazioni

Sono presenti vari tipologie di stazioni, differenziate dalla loro dimensione.

Stazioni più grandi risulteranno avere un numero maggiore di viaggiatori partenti e arrivanti durante la simulazione.

### Binari e Treni

La rete ferroviaria prevede varie livelli di velocità di viaggio, caratterizzati da treni e binari adatti a tali velocità. Saranno disponibili quindi varie tipologie di questi componenti a seconda della velocità di linea che si vuole creare.

Ogni treno e binario presenterà dei costi maggiori all'aumentare della velocità e capienza e sarà un altro aspetto da considerare durante la modellazione della rete.

### Viaggiatori

I viaggiatori all'interno della simulazioni saranno utenti con la necessità di raggiungere una determinata stazione di destinazione partendo dalla loro stazione di partenza. Ogni viaggiatore può scegliere in maniera casuale se preferire le soluzioni che portano a destinazione nella maniera più rapida o nella maniera più economica.

### Guasti

Durante la simulazione è possibile che si formino dei guasti che bloccano una determinata linea ferroviaria e comportino dei cambi di rotta da parte dei viaggiatori. Ciascun guasto avrà una durata limitata, al termine della quale si considererà risolto e la linea potrà ricominciare a circolare normalmente.

Tali guasti incoraggeranno la creazione di una rete ferroviaria ridondante e con pochi punti di singola rottura.

## Requisiti funzionali
### Utente

Utilizzando l'applicazione, l'utente deve essere in grado di:

*   creare una rete ferroviaria;
    *   scegliere le tipologie di stazioni e treni;
    *   posizionare le stazioni su una mappa;
    *   collegare le stazioni tramite binari;
    *   pianificare le tratte dei treni in termini di stazioni visitate;
*   gestire la simulazione;
    *   definire un numero di giorni da simulare;
    *   interrompere la simulazione.
*   controllare l'efficienza della rete modellata;
    *   ottenere un report con le statistiche a fine simulazione

### Sistema

I requisiti del sistema sono:

*   modellazione del tempo per progressione della simulazione e per la stima dei tempi di percorrenza;
*   visualizzare in maniera testuale la progressione della simulazione;
*   gestire eventuali sovrapposizioni di tratte tra i treni per evitare scontri;
*   generare un report finale con le statistiche della simulazione.

## Requisiti non funzionali

*   **Estendibilità**: il sistema deve essere essere facilmente ampliabile con nuovi tipi di treni, binari e stazioni.
*   **Usabilità**: l’interfaccia grafica deve essere minimale e intuitiva, richiedendo un numero limitato di click per completare operazioni comuni come creare stazioni, collegarle, avviare e interrompere la simulazione.
*   **Portabilità**: il sistema deve essere eseguibile su diversi sistemi operativi quali Windows e Linux.
*   **Affidabilità**: il sistema deve gestire correttamente errori di input e condizioni limite, ad esempio cicli infiniti nella rete ferroviaria, senza causare crash.
*   **Trasparenza simulativa**: durante la simulazione, il sistema deve fornire una visualizzazione testuale chiara che permetta all'utente di comprendere facilmente lo stato della simulazione.

## Requisiti di implementazione

L'implementazione del sistema deve utilizzare:

*   Scala 3;
*   Prolog;
*   JDK 21+ (TODO se usiamo Java);
*   SBT;
*   (TODO) per l'interfaccia.