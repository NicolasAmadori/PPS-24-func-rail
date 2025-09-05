---
title: Processo di sviluppo
nav_order: 2
parent: Report
---
# Processo di sviluppo

# Scrum

Per lo sviluppo si è deciso di applicare una metodologia di lavoro SCRUM-inspired, suddividendo il lavoro in sprint e monitorando l'avanzamento dei lavori tramite i *backlog* e i report.

Nel primo incontro sono stati definiti:

- la durata degli sprint a 1 settimana;
- la distribuzione dei meeting, prevedendo uno *Sprint Planning* iniziale, un *Daily Scrum* giornaliero e una fase finale che comprenda il *Product Backlog Refinement*, la *Sprint Review* e il *Sprint Retrospective*;
- i ruoli dei membri del gruppo: Amadori Nicolas sarà il product owner mentre Nanni Denise ricoprirà il ruolo di SCRUM Master;
- la *definition of done* stabilendo che il codice risulti completo solo dopo averlo ben documentato tramite scaladoc e aver sviluppato dei test completi e che eseguano con successo.

# Versioning

Come strumento di gestione del versioning sono stati scelti Git e GitHub, utilizzando GitFlow per garantire una gestione del flusso di lavoro standard e definita e le Pull Request per mantenere un controllo diretto sulle modifiche effettuate al branch principale.

La modalità di numerazione delle versioni si basa sul *versioning semantico* quindi segue la struttura MAJOR.MINOR.PATCH.

# Continuous integration e delivery

Per mantenere una buona affidabilità del software, si è scelto di utilizzare GitHub Actions per creare dei workflow che rieseguano i test sull'intero codice e che ne effettuino controlli sullo stile, oltre che gestire la creazione delle release.

Il workflow gestisce anche la generazione dei report relativi alla copertura del codice da parte dei test.

# Strumenti utilizzati

- Git
- GitHub
    - GitHub Actions
- ScalaTest per lo sviluppo dei test
- Test coverage
- Scalaformatter e Scalafix