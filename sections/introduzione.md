# Introduzione

Con il progetto Functional Rail ci poniamo l'obiettivo di realizzare un simulatore di rete ferroviaria tramite l'utilizzo della programmazione funzionale in linguaggio Scala.

Tramite una GUI, sarà possibile definire la struttura della rete posizionando le stazioni, i binari e i treni per poi fare partire una simulazione impostando parametri quali i numero di giorni da simulare.

Più nel dettaglio saranno presenti varie categorie di stazioni, differenziate dall'affluenza collegate, tra loro da binari che permettono il transito di treni di due tipologie: normali o ad alta velocità.

Durante la simulazione saranno generati dei viaggiatori che avranno la necessità di raggiungere una determinata stazione. L'esecuzione consisterà nel simulare lo spostamento dei treni lungo le tratte predefinite, gestendo eventuali precedenze e congestioni.

Al termine della simulazione verrà generato un report contenente diverse statistiche sull'efficienza e la copertura della rete, tenendo in considerazione i tempi di percorrenza dei viaggiatori e la percentuale di viaggiatori che riesce a raggiungere la destinazione desiderata.