# Processo di sviluppo

## Scrum

Per lo sviluppo si è deciso di applicare una metodologia di lavoro SCRUM-inspired, suddividendo il lavoro in sprint e monitorando l'avanzamento dei lavori tramite i _backlog_ e i report.

Nel primo incontro sono stati definiti:

*   la durata degli sprint a 1 settimana;
*   la distribuzione dei metting, prevedendo uno _Sprint Planning_ iniziale, un _Daily Scrum_ giornaliero e una fase finale che comprenda il _Product Backlog Refinement_, la _Sprint Review_ e il _Sprint Retrospective;_
*   i ruoli dei membri del gruppo: Amadori Nicolas sarà il product owner mentre Nanni Denise ricoprirà il ruolo di SCRUM Master;
*   la _definition of done_ stabilendo che il codice risulti completo solo dopo averlo ben documentato tramite scaladoc e aver sviluppato dei test completi e che eseguano con successo;
*   l'utilizzo di ClickUp come strumento di supporto all'applicazione della metodologia di lavoro agile.

## Versioning

Come strumento di gestione del versioning sono stati scelti Git e GitHub, utilizzando GitFlow per garantire una gestione del flusso di lavoro standard e definita e le Pull Request per mantenere un controllo diretto sulle modifiche effettuate al branch principale.

La modalità di numerazione delle versioni si basa sul _versioning semantico_ quindi segue una struttura MAJOR.MINOR.PATCH.

## Continuous integration e delivery

Per mantenere una buona affidabilità del software, si è scelto di utilizzare GitHub Actions per creare dei workflow che rieseguano i test sull'intero codice e che ne effettuino controlli sullo stile, oltre che gestire la creazione delle release.

## Strumenti utilizzati

*   Git
*   GitHub
    *   GitHub Actions
*   ClickUp per la gestione dei task
*   Scalatest per lo sviluppo dei test
*   Scalaformatter TODO complete