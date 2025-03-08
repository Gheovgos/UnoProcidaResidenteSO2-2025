# Operating Systems 24/25 - Uno Procida residente

## 📌 Descrizione  
Questa repository contiene un'applicazione per la gestione dei traghetti, sviluppata nell'ambito del corso di **Sistemi Operativi** (A.A. 2024/2025). Il progetto è un **fork di [UnoProcidaResidente](https://github.com/reverse-unina/UnoProcidaResidente)**, adattato per specifiche esigenze didattiche.  

L'applicazione permette di:  
- Visualizzare gli orari dei traghetti.  
- Gestire prenotazioni e disponibilità.  
- Simulare il funzionamento del sistema di trasporti marittimi.  

## Issue
L’applicazione di esempio si basa su AsyncTask, soluzione ormai considerata obsoleta e quindi deprecata.
Gli AsyncTask servono ad avviare in parallelo, all’apertura dell’applicazione, la lettura degli aggiornamenti degli orari da file, la lettura dei dati meteo aggiornati, la lettura del file con le segnalazioni fatte dagli utenti .

Provare a migrare I tre servizi in un’altra forma più aggiornata, mantenendo le funzionalità e le prestazioni (tempo di prima apertura, tempo di risposta) e gestendo i casi eccezionali.

Gli orari dei traghetti provengono da fonti eterogenee e sono aggiunti manualmente nel file che raccoglie tutti gli orari
Molte compagnie forniscono aggiornamenti agli orari e soprattutto indicazioni su annullamenti e ritardi delle corse nei propri siti Web

Una funzionalità aggiuntiva potrebbe riguardare l’interrogazione di queste risorse per poter avere indicazioni in tempo reale sugli aggiornamenti
Il componente che interroga potrebbe essere esterno all’app Android e andare a scrivere sul file degli orari oppure più semplicemente sul file delle segnalazioni
Da notare che la scrittura sul file delle segnalazioni è al momento realizzato da un file php al quale vengono inviati i dati della segnalazione da aggiungere con una chiamata http GET

Questa modifica potrebbe essere realizzata nell'ambito di un componente separato che viene deployato su una risorsa server anzichè sul client Android, per minimizzare il numero di richieste, oppure sul client, per consentire l'esecuzione di richieste urgenti.

## Autori
Giorgio Longobardo N97/478
Claudio Riccio N97/483
