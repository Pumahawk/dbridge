# DBridge

- [DBridge](#dbridge)
  - [Overview](#overview)
  - [Features](#features)
  - [Esempio endpoint articles](#esempio-endpoint-articles)
  - [Quickstart](#quickstart)
    - [Prerequisiti](#prerequisiti)
    - [Configurazione e avvio demo](#configurazione-e-avvio-demo)
  - [Configurazione](#configurazione)
    - [File di configurazione dbridge.yaml](#file-di-configurazione-dbridgeyaml)
      - [Configurazione dbridge-config](#configurazione-dbridge-config)
      - [Configurazione global-validator](#configurazione-global-validator)
      - [Configurazione query](#configurazione-query)
    - [Collegamento database con HikariCP](#collegamento-database-con-hikaricp)
  - [Dettaglio oggetti configurazione](#dettaglio-oggetti-configurazione)
    - [Validator](#validator)
    - [Schema](#schema)
  - [Librerie esterne](#librerie-esterne)
  - [Dettaglio SPEL](#dettaglio-spel)
    - [Utilizzo Bean in SPELL](#utilizzo-bean-in-spell)
  - [Creazione query SQL usando Velocity](#creazione-query-sql-usando-velocity)
    - [Funzionamento elaborazione query](#funzionamento-elaborazione-query)
    - [Velocity variabile di supporto **$\_**](#velocity-variabile-di-supporto-_)
    - [Velocity configurazione](#velocity-configurazione)

## Overview

**DBridge** semplifica la creazione di servizi REST che restituiscono informazioni memorizzate
in un database.

Permette di creare delle query **SQL dinamiche** e mappare le informazioni estratte in un **documento JSON** ben strutturato.

## Features

- **SQL dinamico** sfruttando il template engine [Velocity](https://velocity.apache.org/engine/1.7/user-guide.html).
- **Linguaggio di scripting** [SPEL](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#expressions) per la validazione dei parametri di request e la creazione dello schema di response.
- **Schema** in grado di generare un documento annidato di response partendo dal risultato della query sfruttando SPEL. 

## Esempio endpoint articles

L'esempio ha lo scopo di mostrare la versatilità nell'utilizzo del template engine nella creazione delle 
query SQL e dell'utilizzo di *SPEL* come strumento nella creazione dello schema.

Il programmatore ha la completa libertà nel decidere come organizzare il proprio codice SQL e nel poter
creare le proprie [macro](https://velocity.apache.org/engine/1.7/user-guide.html#velocimacros). 

Per una completa comprensione del seguente esempio è consigliato leggere le configurazioni di una demo
presente nei test [end to end](https://github.com/Pumahawk/dbridge/tree/master/code/dbridge-core/src/test/resources/endtoend).

L'endpoint `/articles` permette di effettuare una ricerca sugli articoli usando i filtri opzionali:

- id
- title
- author

Inoltre è possibile sfruttare la paginazione usando i parametri

- limit
- offset

Per la creazione dell'API *articles* è possibile creare la seguente configurazione.

```yaml
# config/articles.dbridge.yaml

# File di configurazione della query contenete la query in velocity, i validatori e lo scheama di response

kind: query
metadata:
  name: articles
spec:
  path: "/articles"
  queries:
  - sql: "#parse('/articles/list.vm')"
  validators:
  - _extends: search
  schema:
    _input: "{rows:#input}"
    total: "#input['rows'][0]['TOTAL']"
    rows:
      _input: "#input['rows'][0]['ARTICLE_ID'] != null ? #input['rows'] : {}"
      id: "#input['ARTICLE_ID']"
      title: "#input['ARTICLE_TITLE']"
      author:
        name: "#input['USER_NAME']"
        links:
          self: "'/users/' + #input['USER_ID']"
      links:
        self: "'/articles/' + #input['ARTICLE_ID']"
```

Per migliorare l'organizzazione dei file è possibile scrivere la query in un file separato.

```sql
-- velocity-template/articles/list.vm

#@search()
    #parse('/articles/article.vm')
    WHERE
        1 = 1
        #ifAndEqual($id '"ARTICLES"."ID"')
        #ifAndLike($title '"ARTICLES"."TITLE"')
        #ifAndLike($author '"USERS"."NAME"')
#end

```

Chiamando l'API è possibile ottenere il seguente risultato.

```bash
curl -G localhost:8080/articles -dtitle=fir
```

```json
{
  "data": {
    "total": 2,
    "rows": [
      {
        "author": {
          "name": "Marco",
          "links": {
            "self": "/users/1"
          }
        },
        "links": {
          "self": "/articles/1"
        },
        "id": 1,
        "title": "First"
      },
      {
        "author": {
          "name": "Enrico",
          "links": {
            "self": "/users/2"
          }
        },
        "links": {
          "self": "/articles/3"
        },
        "id": 3,
        "title": "First"
      }
    ]
  }
}
```

## Quickstart

Per iniziare ad utilizzare DBridge è possibile fare riferimento al branch *demo-quickstart*.

### Prerequisiti

Per seguire la seguente guida è necessario avere le seguenti dipendenze installate:

- Git
- Java 8

> E' possibile eseguire velocemente una istanza postgres direttamente con il comando docker  
> `docker run -d --name postgres-demo -p 5432:5432 -e POSTGRES_PASSWORD=postgres postgres`

### Configurazione e avvio demo

- Clonare il branch *demo-quickstart*

```bash
git clone --depth 1 --branch demo-quickstart https://github.com/Pumahawk/dbridge.git dbridge-demo
```

- Modificare gli accessi al database nel file *application.properties*

```
mydb.jdbcUrl=jdbc:postgresql://localhost/
mydb.driverClassName=org.postgresql.Driver
mydb.username=postgres
mydb.password=postgres
```

- Eseguire dbridge

```bash
# Avvia dbridge utilizzando lo script

./dbridge

# oppure direttamente tramite comando java
java -Dloader.path=./lib -jar dbridge.jar
```

- Effettuare la chiamata GET all'api `/version`

```bash
curl localhost:8080/version
```

Se l'applicativo è stato configurato correttamente ci si aspetta il seguente output.

```json
{
  "data": {
    "version": "PostgreSQL 15.1 (Debian 15.1-1.pgdg110+1) on x86_64-pc-linux-gnu, compiled by gcc (Debian 10.2.1-6) 10.2.1 20210110, 64-bit"
  }
}
```

---

## Configurazione

DBridge si può dividere nelle seguenti componenti principali.

- **.dbridge.yaml** - File di configurazione specifici di DBridge.
- **HikariCP** - Componente di accesso al database.
- *SPEL* - Semplice linguaggio di scripting.
- **Velocity** - Template engine.

### File di configurazione dbridge.yaml

Per poter sfruttare le principali funzionalità di DBridge bisogna creare e configurare dei file che terminano con l'estensione *.dbridge.yaml*.

Ogni file e strutturato in questo modo:

```yaml
kind: string
metadata:
  name: string
spec:
  ...
```

Sono presenti i seguenti tipi di file:

- dbridge-config - Configurazioni generiche di DBridge come l'accesso al database.
- global-validator - Lista di validatori che possono essere riutilizzati all'interno degli endpoint.
- query - Funzionalità che associa un'API REST ad una query SQL.


#### Configurazione dbridge-config

Kind: **dbridge-config**

Permette alcune confiugurazioni di sistema come il collegamento al database.

- **name**: Nome del collegamento, può essere utilizzato all'interno delle query per indicare su quale database bisogna effettuare l'operazione.
- **configurationId**: Prefisso property di Spring che permette di configurare l'oggetto
  *HikariDataSource*. Per maggiori informazioni leggere il [paragrafo dedicato](#collegamento-database-con-hikaricp).
- **default**: Valore che indica il database da utilizzare nel caso non venga specificato nella query.

```yaml
spec:
  database:
  - name: database-1
    configurationId: prefix.custom.property1
    default: true
  - name: database-2
    configurationId: prefix.custom.property2
```

#### Configurazione global-validator

Kind: **global-validator**

Permette di raggruppare dei validatori per poi riutilizzarli all'interno delle query.

Permettono di effettuare operazioni di controllo e conversione dei dati in input prima che vengano passati
alla query.

- **name**: Nome del gruppo di validatori. Può essere richiamato all'interno della query.
- **validators**: Elenco di validatori. Per maggiori informazioni leggere la [sezione dedicata](#validator).

```yaml
spec:
  globalValidators:
  - name: group-name
    validators:
    - name: validator-1
      _input: "#p['id']" # order 1
      convert: "#p['id'] = #toNumber(#input)" # order 3
      validator:
        spel: "#input eq null || #isNumber(#input)" # order 2
        message: "Id must be a valid number. Current value: #{#input}"

```

#### Configurazione query

Kind: **query**

Creazione di un endpoint che mette in relazione una chiamata http con una query SQL.

Inoltre permette di definire dei validatori per la request e uno schema per la response.

- **path**: Uri path utilizzato durante la richiesta http.  
  Il suo funzionamento sfrutta la classe [UriTemplate](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/util/UriTemplate.html)
  di Spring e per questo si possono sfruttarne alcune funzionalità come la definizione di 
  parametri e utilizzare le regex.  
  Es:
  - */users* 
  - */users/{id}* 
  - */users/{id:[0-9]+}* 
- **queries.name**: Nome della query utilizzabile all'interno delle espressioni *SPEL* e *Velocity*.
- **queries._input**: Funzione *SPEL* da utilizzare per creare la variabile *input* all'interno di *Velocity*.
- **queries.conversion**: Funzione per modificare il valore della variabile *result* dopo la query.
- **queries.sql**: Inserimento di una query SQL che verrà processata dal template engine *Velocity*.
  E' possibile inserire direttamente una query nel file di configurazione oppure in un file esterno per
  migliorare l'organizzazione delle proprie query SQL.  
  Es:
```yaml
queries:
- name: name-query
  _input: spel-expression
  conversion: spel-result-expression
  sql: |
    SELECT *
    FROM USERS
    WHERE id = :id
```
```yaml
queries:
- sql: "#parse('/users-by-id.vm')"
```

- **query.database**: Nome del database scelto in fase di configurazione. Se non specificato viene utilizzata la configurazione di default.
- **validators**: Elenco di [validatori](#validator).
- **schema**: [Schema](#schema) che mappa il risultato della query con la risposta HTTP.

```yaml
spec:
  path: "/users/{id:[0-9]+}"
  queries:
  - database: "database-connection-name" # optional
    sql: "#parse('/users/byId.vm')"
  validators:
  - _extends: byId
  schema:
    _input: "#foundFirst(#group(#input, 'USER_ID'), 'User not found')"
    id: "#input['USER_ID']"
    name: "#input['USER_NAME']"
    articles:
      _input: "#input.nested.?[#this['USER_ID'] != null]"
      id: "#input['ARTICLE_ID']"
      title: "#input['ARTICLE_TITLE']"
    links:
      self: "'/users/' + #input['USER_ID']"
```

### Collegamento database con HikariCP

La connessione al database avviene attraverso [HikariCP](https://github.com/brettwooldridge/HikariCP) 
permettendo un buon grado di configurazione sul collegamento.

Nel dettaglio, DBridge sfrutta la capacità di Spring di istanziare un oggeto Java partendo dalle properties.
Per questo è possibile definire tutte le properties che sono in grado di sfruttare i metodi setter della
classe [HikariDataSource](https://www.javadoc.io/doc/com.zaxxer/HikariCP/2.7.8/com/zaxxer/hikari/HikariDataSource.html).

Tutte le possibili configurazioni di *HikariCP* è possibile consultarle sulla
[documentazione ufficiale](https://github.com/brettwooldridge/HikariCP#gear-configuration-knobs-baby).

Sfruttando i numerosi metodi per configurare le properties da Spring, consultanto la guida
[Externalized Configuration](https://docs.spring.io/spring-boot/docs/2.1.9.RELEASE/reference/html/boot-features-external-config.html),
si possono definire le properties attraverso file di configurazione specifici per ambiente (application.properties, application-dev.properties, application-prod.properties), tramite variabili d'ambiente o system properties.

Es:

Configurazione al database:

```yaml
kind: dbridge-config
metadata:
  name: documentation-guide
spec:
  database:
  - name: mydatabase
    configurationId: my.properties.custom.path
    default: true
```

File di properties:

```txt

my.properties.custom.path.jdbcUrl=jdbc:postgresql://localhost/blog
my.properties.custom.path.driverClassName=org.postgresql.Driver
my.properties.custom.path.username=postgres
my.properties.custom.path.password=${DB_PASSWORD}

```

Variabili di ambiente:

- DB_PASSWORD: password di accesso al database

Tutti i valori delle properties sfruttano dei metodi della classe HikariDataSource:

- jdbcUrl => setJdbcUrl(String)
- driverClassName => setDriverClassName(String)
- username => setUsername(String)
- password => setPassword(String)


## Dettaglio oggetti configurazione

Gli oggetti di configurazione possono pessere utilizzatati in determinate sezioni nei file di 
configurazione.

A seguire un elenco degli oggetti più utilizzati e il loro schema.

### Validator

Validatore utilizzato per eseguire dei controlli sui dati in input.

- **name**: Nome del validatore.
- **_input**: Funzione SPEL, permette di crare la variabile *#input* con il ritorno
   della funzione SPEL. Questo semplifica la componente di validazione e di 
   conversione.
- **validator.spel**: Funzione *SPEL* che deve restituire un booleano. Se la funzione 
  restituisce un valore falso l'applicativo risponderà con *400 Bad Request*.
- **validator.message**: Messaggio di risposta del validatore in caso di *400 Bad Request*.

```yaml
name: id
_input: "#p['id']"
convert: "#p['id'] = #toNumber(#input)"
validator:
  spel: "#input eq null || #isNumber(#input)"
  message: "Id must be a valid number. Current value: #{#input}"
```

### Schema

Lo schema permette di strutturare la risposta.

E' uno strumento molto versatile che fa un uso importante del campo *_input* per sfruttare al meglio le
sue funzionalità.

Un oggetto di tipo schema ha una struttura ricorsiva. Ogni suo elemento, ad eccezione di *_input* sono di tipo schema.

Un oggetto di tipo schema può essere una stringa. In questo caso si sta specificando una istruzione *SPEL* che verrà eseguita per recuperare il valore di un determinato campo.

*DBridge* fa un uso importante della libreria [Jackson](https://github.com/FasterXML/jackson) per ricostruire la risposta del server partendo dall'oggetto restituito dalla funzione *SPEL*.

Il campo **_input** è un campo speciale che permette di specificare una funzione *SPEL* che restituirà
l'oggetto assegnato alla variabile **#input** utilizzabile all'interno delle istruzioni *SPEL* di tutti gli oggetti annidati.

Durante la conversione dell'oggetto restituito dalla funzione di *_input* oppure della funzione di *SPEL*
è importante sapere che se l'oggetto implementa l'interfaccia **iterable** allora la risposta verrà convertità in una lista e lo schama fa riferimento ai singoli oggetti.

> Il risultato di una query è sempre una lista. Per convertirlo in oggetto è possibile crearlo tramite le istruzioni di *SPEL*.

**Esempi**

*Restituire un oggetto contenente un messaggio*

```yaml
schema:
  _input: "{:}" # Oggetto vuoto
  message: "'Hello, World'"
```
 **Output**: `{"data":{"message":"Hello, World"}}`


*Restituire un oggetto contenente una lista di messaggi*

```yaml
schema: # Siccome _input è una lista, lo schema fa riferimento ai singoli oggetti della lista
  _input: "{'Hello World','Bonjour le monde!'}" # Oggetto vuoto
  message: "#input"
```
 **Output**: `{"data":[{"message":"Hello World"},{"message":"Bonjour le monde!"}]}`

*Processare il risultato della query in un json strutturato*

```yaml
# Query SQL: SELECT version()

schema:
  _input: "{message: 'This is a response', rows:#input}" # Lo schema principale sarà un oggetto
  message: "#input['message']"
  rows:
    _input: "#input['rows']" # Il campo rows sarà una lista
    value: "#input['version']"
```

```json
{
  "data": {
    "message": "This is a response",
    "rows": [
      {
        "value": "PostgreSQL 15.1 (Debian 15.1-1.pgdg110+1) on x86_64-pc-linux-gnu, compiled by gcc (Debian 10.2.1-6) 10.2.1 20210110, 64-bit"
      }
    ]
  }
}
```

## Librerie esterne

Per aggiungere i driver di connessione al database oppure aumentare le funzioni disponibili di *SPEL* è
 possibile definire una cartella in cui andare ad inserire i file jar contenente il codice necessario 
 all'applicativo per funzionare.

Questa funzionalità è resa possibile grazie a *Spring* e bisogna definire la variabile d'ambiente
**loader.path** con il percorso alla cartella desiderata.

Es: `java -Dloader.path=./custom-lib -jar dbridge.jar`

## Dettaglio SPEL

Spel è stato integrato all'interno di *DBridge* per sfruttarne le funzionalità.

E' possibile creare delle funzioni custom tramite file di properties in questo modo:

```txt
spel.methods.isNumber=org.apache.commons.lang3.math.NumberUtils,isParsable,java.lang.String
```

La property sopra pemette di creare la funzione #isNumber utilizzabile all'interndo dei validatori
e dello schema.

Per consultare le funzioni aggiunte di default è possibile consultare il file [spel.properties](/code/dbridge-core/src/main/resources/spel.properties)

### Utilizzo Bean in SPELL

E' possibile accedere ai Bean di Spring direttamente dal codice Spel.

```
{message: 'Text: ' + @beanName.getText()}
```

## Creazione query SQL usando Velocity

**Velocity** consiste in un template engine e il suo scopo è quello di permettere la creazione di un testo
senza alcuna limitazione sul modo in cui il testo viene composto.

Per questo motivo è importante sapere come scrivere una query per evitare di incorrere in errori comuni
come il rischio di permettere l'SQL Injection.

Su DBridge Velocity è stato configurato per poter utilizzare delle funzionalità utili alla scrittura di
codice SQL.

 Per la guida completa è consigliato visitare il sito ufficiale su
 ["User Guide - Contents"](https://velocity.apache.org/engine/2.3/user-guide.html).

### Funzionamento elaborazione query

DBridge ha integrato **Velocity** e **NamedParameterJdbcTemplate** per semplificare la scrittura della 
query.

Tutte le variabili della request (come i query parameters) e quelle create dai validatori possono essere 
usate all'interno di **Velocity** e **NamedParameterJdbcTemplate**.

Esistono delle properties speciali che migliorano la gestione delle variabili:

- **$_** : Oggetto di supporto.
- **$_s** : Recupero dei query parameters sottoforma di lista. Utile se si vogliono utilizzare più
   parametri contemporaneamente.

**Filtrare utenti per codice fiscale**

Il codice fiscale viene passato come query params nella variabile *cf*.  
Può essere utilizzato all'interno della query grazie a **NamedParameterJdbcTemplate** scrivendo `:cf`.

`curl http://localhost:8080/users?cf=xxxxxxxxxxxx`

```sql
SELECT * FROM USERS WHERE CF = :cf
```

**Recuperare i film appartenenti ad almento una categoria tra quelle specificate**

Le categorie vengono passate più volte in input e per questo bisogna utilizzare la variabile speciale $_s e $_.

`curl https://localhost:8080/movies?category=action&category=horror`

```sql
SELECT * FROM MOVIES WHERE CATEGORY IN ( $_.use($_s['category']) )
```

### Velocity variabile di supporto **$_**

All'interno del codice di Velocity è possibile utilizzare la variabile di supporto **$_**.

Questa variabile può essere utilizzata per semplificare operazioni che altrimenti sarebbero molto
complesse da riprodurre manualmente su Velocity.

- ```$_.use( parameter )``` - Permette l'inserimento di un oggetto Java all'interno della query evitando l'sql Injection.

### Velocity configurazione

E' possibile configurare velocity direttamente tramite il file di **application.properties**.

Per modificare le properties indicate in [documentazione](https://velocity.apache.org/engine/2.0/configuration.html) basta 
aggiungerle nel file di application properties con il prefisso **velocity.**.

**ES: modifica varibile [set null allowed](https://velocity.apache.org/engine/2.0/configuration.html#set-directive)**

```txt
velocity.directive.set.null.allowed=true
```