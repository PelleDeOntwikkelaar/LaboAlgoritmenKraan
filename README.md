# LaboAlgoritmenKraan

Er staat een lege slot tussen de inputslot en de opslagplaats, dit ook voor de output.

We overlopen eerst de output jobs en dan de input jobs. Als de outputcontainer 
niet aanwezig is controleren we de inputjobs. Als we de outputjob niet direct kunnen
doen dan maken we nieuwe jobs in een priority queue die we eerst uitvoeren.

Hoe gaan we de beste plaats kiezen???

De x en y beweging kan tegelijk gedaan worden.

## Kraanopdracht 1

* 1 kraan
* containers staan **recht** op elkaar

### Hoe de data opslaan?

Voor in- en outputsequenties gebruiken we een linked list

Een lijst van rijen.
Per rij een lijst geordend volgens niveau.

Available slots bijhouden?




