---
title:Introduzione
nav_order: 1
parent: Report
---
# Introduzione

Con il progetto Functional Rail ci poniamo l'obiettivo di realizzare un simulatore di rete ferroviaria tramite l'utilizzo della programmazione funzionale in linguaggio Scala.
Tramite una GUI sarà possibile definire la struttura della rete posizionando le stazioni e i binari, creare dei treni specificando le loro caratteristiche e in fine fare partire una simulazione impostando i vari parametri.
Nella creazione della rete sarà possibile posizionare stazioni grandi o piccole, differenziate da dimensioni e affluenza, collegarle con binari che, a seconda del loro materiale, permetteranno il transito di treni normali o ad alta velocità.
Una volta definita la rete ferroviaria, sarà possibile aggiungere i treni scegliendo il loro tipo, la loro stazione di partenza e le loro fermate, e impostare parametri quali i numero di giorni da simulare, la velocità di simulazione, la presenza di guasti delle rotaie e il criterio di scelta degli itinerari da parte dei passeggeri.
Durante la simulazione saranno generati dei viaggiatori che avranno la necessità di percorrere un determinato itinerario. L'esecuzione consisterà nel simulare lo spostamento dei treni lungo le tratte predefinite, gestendo eventuali precedenze e guasti.
Al termine della simulazione verrà generato un report contenente diverse statistiche sull'efficienza e la copertura della rete, tenendo in considerazione i tempi di percorrenza dei viaggiatori e la percentuale di viaggiatori che riesce a raggiungere la destinazione desiderata.