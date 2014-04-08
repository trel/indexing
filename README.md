Indexing Framework
========

An indexing framework for iRODS based middleware

Overview
--------
The indexing framework comes in an OSGI bundle that provides two services

    databook.listener.service.MessagingService
    
and

    databook.listener.service.IndexingService
    
The former is used to interface with messaging services such as AMQP 0.9, AMQP 1.0, etc. This can be done by defining a Camel route.

The latter is used to interface with indexers. IndexingService provides methods for an indexer to register and unregister. When an indexer is registered to the IndexingService, the IndexingService will relay all messages that it receives to the indexer in the form of POJO. The indexer also acquires a reference to a scheduler automatically which allows the indexer to submit time-consuming tasks to be scheduled. Currently, the supported tasks are those which require access to the underlying data grid. Each task is composed of an object indicating which data object it needs to access and a continuation object which will be called by the scheduler when the data object and system resources become available. A simple scheduler based on a thread pool model is included. More complex schedulers can be implemented by implementing the 

    databook.listener.Scheduler
    
interface.

Installation
--------

* Install Apache ServiceMix 5.0.0

* Checkout indexing framework source from github

* Run

        mvn install
        
  This will generate an OSGI bundle in the maven project target directory
  
* Deploy generated target to ServiceMix 5.0.0


Message Format
--------

The message format is defined in JSON Schema. Look for the schema directory in the source tree for the schemas. The jsonschema2pojo plugin is used in the build process to generate Java classes corresponding to the schemas. Jackson is used to translate between JSON messages and Java objects. Indexers do not need to parse JSON, the messages are passed into indexers as Java objects, and fields of JSON can be accessed from fields of the Java objects.

Configuring ServiceMix with AMQP Routing
--------

Configuring iRODS
--------
Enable the databook.re rule base. In iRODS, edit server/config/server.config. Add "databook" before "core".

This works for the default install where the default rules are empty.

If there are used defined PEP rules in "core", then they will be overriden by the databook PEP rules. In this case the databook rules needs to be merged with the "core" rule.

First, rename pep rules. For example, rename

    acPostProcForPut
    
to

    postProcForPut
    
This will prevent databook rules from overriding PEP rules defined in core.

Then, add call to the databook rules from used defined PEP rules. For example, if you have a rule

    acPostProcForPut {
        ...
    }
    
then add call to data book rules

    acPostProcForPut {
        ...
        postProcForPut
    }
    
    

Indexers
--------

### ElasticSearch ###

The ElasticSearch indexer comes in a seperate OSGI bundle.

### VIVO ###

The VIVO indexer comes in a seperate OSGI bundle.

Advanced Topics
========

Composing JSON Messages Using the Java DSL
--------
The Java DSL for composing JSON Message is generated from an EriLex specification. This DSL allows very flexible composition of JSON Messages against the schemas. Some examples:

    .object()
        .key("key1").value("value1")
        .key("key2").value("value2")
        .key("key3").value("value3")
    .end()

and

    .object()
        .key("key1").array()
            .elem("elem1")
            .elem("elem2")
            .elem("elem3")
        .end()
    .end()

and

    .object()
        .key("key1").array()
            .object("elem1")
                .key("key1").value("key1")
            .end()
            .object("elem2")
                .key("key2").value("key2")
            .end()
            .object("elem3")
                .key("key3").value("key3")
            .end()
        .end()
        .key("key2").value("value2")
        .key("key3").value("value3")
    .end()

Objects and arrays can be nested arbitrarily.


Rule-based Mapping of Java Objects to Indexes
--------

Some indexers, such as triple store or graph database based indexers, have to serialize Java objects into textual representation in a specific format. This is supported by Rule-base mapping in the indexing framework.

For example, suppose we have a JSON object

    {
        "type": "DataObject"
        "label": "DataObject1"
        "title": "DataObject2"
    }
    
But we want to index the "label" field and the "title" field differently, then we can define two rules. The rules match fields in a similar way Java resolves ad hoc polymorphism. This is possible because in the JSON schemas subtype hierachies are defined.

The rules are defined by implementing the ObjectPropertyRule interface. There are five methods to implement:

    create, delete, modify, union, diff
    
There semantics are as following:
For non-array properties:
* create(v): Set the value of a previous undefined property
* delete(v): Reset the value of a property, making it undefined
* modify(v): Change the value of a property, if the new value is null, then the property becomes undefined
* union(v): If the property is undefined, then set it to v
* diff(v): If the property is defined and equal to v, then reset the property, making it undefined

For array properties:
(We do not distinguish between undefined and empty array)
* create(v): Set the value of a previous undefined property
* delete(v): Reset the value of a property, making it undefined
* modify(v): Changes the value of a property, if the new value is null, then the property becomes undefined
* union(v): Set the property to the union of its current value and v
* diff(v): Set the property to the difference of its current value and v

The defined rules can be registered to a RuleSet object.


