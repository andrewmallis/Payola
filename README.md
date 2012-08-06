# We &hearts; Payola!
---
# Setting up Payola
## System Requirements

Payola requires a [Scala](http://www.scala-lang.org) environment, which is supported on virtually any platform capable of running Java code - both Unix and Windows-based systems are fully supported. The system should have at least 1GB of memory dedicated to Payola itself.

Aside from the actual Payola server, you need to be capable of running any [Squeryl-compatible](http://squeryl.org) relational database for storing user data and a [Virtuoso](http://virtuoso.openlinksw.com) server for storing personal RDF data. Neither of those need to be necessarily running on the same system as Payola itself (this is configurable in the `payola.conf` file as described later on).

To work with Payola, you'll need a web browser capable of displaying HTML5 web pages. Payola takes advantage of many HTML5 features - keep your web browser up-to-date all the time. Recommended are the *latest versions* of WebKit-based browsers (e.g. Chrome, Safari), Firefox, Opera, or IE. A 1440x900 or larger display is highly recommended.

## Installation Guide

You need to have a working Scala environment to install Payola with [SBT (Scala Build Tool)](https://github.com/harrah/xsbt/wiki/) installed. Clone Payola git repository: `git://github.com/siroky/Payola.git` to a local folder.

### <a name="configuring"></a>Configuring 

Payola comes pre-configured to work with default settings of a Virtuoso server and an H2 database installed on the same server as Payola is running (i.e. localhost). To change this configuration, edit `payola/web/shared/src/main/resources/payola.conf` - it’s a regular text file with various options on each line. Comment lines start with a hash symbol (`#`).

###### TODO: Describe each option in the conf

### Compiling and Running Payola

As you clone just source codes from the git repository, it is necessary to compile Payola. To do so, you need to have SBT installed as noted above. Open command line (console, terminal) and make `payola` subdirectory current working subdirectory. Launch SBT (most likely using the `sbt` command) and enter the following commands:

```
> cp
...
> project initializer
> run
...
> project server
> run
```

Voilà! Your Payola server is running. The `initializer` project sets up your database to include an admin user (login `admin@payola.cz`, password `payola!`), a sample analysis and some data sources. You can, of course, remove those and create your own.

> <a name="drop-create-warning"></a> **WARNING:** The `initializer` project drop-creates necessary tables - hence all previous data will be lost. Run this project only when installing Payola.

### Security

Both the Virtuoso server and H2 database allow by default incoming connections from outside of your network, or localhost - Payola allows users to store their own private RDF data using Virtuoso groups that are identified using a generated 128-bit UUID (and the H2 database is secured by a username-password combination).

While a simple guess of another user's group identifier is unlikely (and a brute-force attack on the username-password combination is highly noticeable), it is advisable to secure your local Virtuoso storage and your relational database by denying all incoming and outgoing connections outside of localhost, or if on a secure company network, outside of that particular network. This is up to each admin to correctly set up the server's firewall.

---
# Using Payola

## Launching

To launch Payola, open SBT just like when you were compiling it and enter these two commands (you do not need to run the `cp` command if you haven't modified any source code since the last compilation):

```
> cp
...
> project server
> run
```

>*Warning:* Do **not** run the `initializer` project. All users, analyses, data sources, etc. would be lost. (See [this note](#drop-create-warning))

Once the server is running, enter the following address in your web browser:

><http://localhost:9000/>

Of course, the port will be different depending on your configuration file (see section [Configuring](#configuring) for details).

## Basic usage

You can use Payola both as a logged in user, or a guest. A guest is limited to analyses and data sources marked as public by other users and only in a read-only mode (i.e. can't edit them).

A logged-in user can create new data sources, analyses, plugins (you can actually write your own plugin, more about that later), edit them and share them; and upload your own private RDF data.

---

### Data Sources

A data source is - as its name hints - a source of data. Payola needs to know where to get its data from for evaluating analyses, etc. - data sources.

##### Creating

When creating a data source, you need to enter a data source name and description, decide whether it's public (then it's visible even to logged out users) and which data fetcher to use.

A data fetcher is a plugin which can communicate with a data source of a specific type. A good example is a `SPARQL Endpoint` data fetcher. SPARQL is a query language for fetching data and such a data fetcher can work with any SPARQL endpoint.

Select a data fetcher of your choice, fill in the data fetcher's parameters (for example, in `SPARQL Endpoint` data fetcher's case an `EndpointURL` parameter) and hit the `Create Data Source` button. You have just created your first data source.

##### Editing

Use the toolbar at the top of the page to list available data sources (click on the `My Data Sources` button and select `View All`).

You can view all data sources available to you. If you wish to edit one (e.g. change its name or description), click on the Edit button on the same row. You'll be redirected to an edit page which contains a delete button as well for removing the data source. The sharing functionality will be described in the [Sharing section](#sharing).

Follow the same steps to list and edit any other entity in the system (analyses, plugins, etc.).

##### Viewing

When on the Dashboard or listing all available data sources, click on a data source to view it.

You'll be presented with a neighborhood of an initial vertex.

Such a subgraph can be viewed in many ways. The default one, presented to you, is a simple table. You can change the visualization plugin using the `Change visualization plugin` button. `Circle`, `Gravity` and `Tree` visualizations will display a regular graph using vertices and edges. 

###### TODO - describe what can be done with a graph

The `Column Chart` visualization will display a column bar graph, but works only with graphs of a specific structure. The graph must have one identified vertex, whose edges are of a `rdf:type` URI, with identified vertex destinations - this destination then must have exactly two edges, both directed to a literal vertex, one with a string value (name of the column), the second one with a numeric value.

###### TODO - a picture of such a graph

##### Ontology Customization

By default, each vertex and edge is of the same color and has the same size (width). To emphasize or diminish some parts of the graph, you can customize the visual appearance using an ontology customization.

While viewing a graph, press the `Change appearance using ontologies` button. If you have already saved some customizations, they'll be listed there - if you haven't created any yet, select the `Create New` menu item. Enter name of the customization, ontology URL and press `Create`.

You will be presented with a customization dialog. On the left, ontology classes are listed - select one. On the right, properties of that class are listed. At the very top of the right column, you can customize the appearance of that class itself (in the graph displayed as vertices), below, you can modify appearance of that property (in the graph displayed as edges).

When done, simply press the `Done` button. If you want to further modify the customization, you can click on the `Edit` button in the `Change appearance using ontologies` button's menu.

---
### Groups

You can create user groups to make sharing easier (as described in the next chapter). Imagine you want to share a resource (e.g. an analysis) to a group of co-workers. One approach would be to share it to each one of them, but this can be tedious considering you might want to share something to them every week or every day. Hence there's a possibility to create user groups - in the top toolbar, click on the `User Groups` button and select `Create New`.

Enter the group name (e.g. 'My co-workers') and hit the `Create Group` button. After the group has been created, you can start adding members to the group. To do so, make the `Members` field active and start typing - the suggestion box will offer users with a matching name. Click on the user to add him or her. If you decide to remove a user, click on the `x` button in front of his or her name. Remember to use the `Save Group` button before leaving the edit page, or all changes made will be lost.

To delete a group, use the `Delete` button at the top-right corner of the page.


[Editing a Group](https://github.com/siroky/Payola/raw/develop/docs/img/group_edit.png)

---
### <a name="sharing"></a>Sharing

Now that you know how to create a group, let's share a data source. In the toolbar, click on the `My Data Sources` button and select `View All`. This lists all your data sources. You can use the `Edit` button to edit the data source, the `Private`/`Public` button to toggle whether the data source is private (only you and people you share it to can use it), or public - anyone can use it, even people who are not logged in.

Then there's the `Share` button. When you click on it, a menu pops up, allowing you to share the data source either to users or groups. When you select the `To Users` menu item, a dialog is shown with a text field which will suggest users as you type just like when you were adding members to a group. 

The other option is to share the data source to groups - again a dialog will appear, letting you select multiple groups using the suggestion box. Add groups you want and hit the `Share` button. All users within the selected groups will be now able to use this data source.

If you no longer want to share a resource with a group or a user, follow the same steps as if you wanted to share it with someone - the dialog which appears will contain the users or groups whom you've shared the resource to previously. Press the `Share` button. The list of users and groups allowed to access the resource will be updated accordingly.

---
### Private Data Storage

While listing data sources, you might have noticed a data source called `Private Storage of ...` - when you sign up, a new private data storage is created in your Virtuoso instance. You can add your own data to this storage. Of course, you can share this data source as well.

##### Adding data to data storage

To add data to your private data storage, use toolbar's `Private RDF Storage` button and select `Upload Data`.

Here you are presented with two options: to upload an RDF/XML or TTL file, or load the RDF/XML from a URL. Retrieving a graph in a TTL format from a URL isn't currently supported.

---
### Analyses

Creating a new analysis is similar to creating any other resource - in the toolbar, select `Create New` from `My Analyses` button's menu. You will be prompted to enter a name - enter the analysis' name - you can change it later on.

You will be presented with a blank page with a control box in the top-left corner. Start by filling in the analysis description.

First, you'll need a data source to start with. You can do so either using the `Add data source` button which will offer you available data sources, or `Add plugin` which will let you add a data fetcher - an anonymous data source. This can be useful if you decide to use a data source that you don't want to save right away (e.g. you know you'll use it just once).

Now that you've added a data source, you need to do something with the data. Click on the `Add Connection` button on your data source box. Payola comes with 6 pre-installed plugins, which will be described one by one below. Of course, you can add your own plugin (see [section Plugins](#plugins)). Plugins are ordered in a row (though more branches can be created, see below) - a plugin  always gets the result of the previous one as its input.

##### Typed

This plugin selects vertices of a type that's filled in as a parameter `TypeURI` from its input graph.

##### Projection

Projection plugin takes property URIs separated by a newline as a single parameter. It will select vertices that are connected to other vertices using one of the listed URIs. 
> **Note:** Payola performs some optimizations, potentially merging several consecutive plugins together. For example, two consecutive projection plugins are merged - hence their result isn't an empty graph as one could expect even if each of them lists completely different set of URIs, but a graph that contains both projections (if this optimization didn't take place, the first plugin would create a graph containing vertices connected to each other using URIs declared in the first plugin, which is then filtered using the second plugin).

##### Selection

Selection plugin lets you select vertices with a particular attribute - for example select cities with more than 2 million inhabitants.

> *Example:* Let's create an analysis which selects all cities with more than 2 million inhabitants. First, add a `DBpedia.org` data source, then connect a new Typed plugin with TypeURI `http://dbpedia.org/ontology/City`. Continue with a Projection plugin with PropertyURIs `http://dbpedia.org/ontology/populationTotal`, then a Selection plugin with PropertyURI `http://dbpedia.org/ontology/populationTotal`, Operator `>` and Value `2000000`. And that's it: your first analysis.

##### Ontological Filter

Ontological Filter plugin filters a graph according to ontologies located at URLs listed in the OntologyURLs parameter.

##### SPARQL Query

This is a more advanced plugin letting you perform your own custom SPARQL query on the output of the previous plugin.

#### Branches

You can add multiple data sources, creating multiple branches that need to be merged before the analysis can be run (trying to run an analysis with branches that aren't merged will yield in an error). Of course, you can have such an incomplete analysis saved and work on it later.

Merging branches can be done using the `Merge branches` button. You will be given a choice to use either Join or Union. After selecting one (each is described below), you need to specify which branches you want to merge - at the bottom of the dialog, there are wells for each input of the merge plugin. At the top of the dialog, you have each branch represented by the name of the last plugin in each branch. If you hover your mouse over the box representing a branch, that particular branch gets highlighted in the background (it gets a thick black frame). You need to drag the branch boxes to the input boxes (see picture attached).

![Branches on Input Boxes](https://github.com/siroky/Payola/raw/develop/docs/img/analysis_branches.png)

##### Union

Union simply merges two graphs together as one would expect. Vertices with the same URI will be unified, their properties merged.

##### Join

Join can be either inner or outer (default).

*Inner join:* Only edges from the first graph with URI defined in the `PropertyURI` parameter are included. Also, the origin of the edge must be present in the second graph. Otherwise, the edge is omitted.

> *Example:* You have two graphs:
>> ###### Graph A
>> `payola.cz/dog` - `payola.cz/barks-at` - `payola.cz/tree`<br/>
>>
>> ###### Graph B
>> `payola.cz/wolf` - `payola.cz/evolved-to` - `payola.cz/dog` 
>>
>
> If graph A is joined with graph B using the `payola.cz/barks-at` property, the `dog - barks-at - tree` triple is included in the result (`payola.cz/dog` is a vertex in graph B).
><br/><br/>
> When tried the other way around - joining graph B with graph A using the `payola.cz/evolved-to` property, an empty graph is returned because `payola.cz/wolf` isn't a vertex in graph A.


*Outer join:* All vertices from the first graph that are origins of edges with URI defined in the `PropertyURI` parameter are included. Moreover, if origin of the edge is included in the second graph, destination of the edge and the edge itself are both included as well.

> *Example:* Using the same graphs as before, merging graph A with graph B will yield in the same result. Merging B with A, however, will include a single vertex `payola.cz/wolf` and no edges.

#### Running Analyses

Either on your dashboard, or on analyses listing, click on an analysis to display details of it. You are presented with an overview of the analysis (which plugins with which parameters and connections are going to be used).

As some analyses can take a really long time to finish (some may be theoretically infinite), there's a timeout field in the top-right corner. By default, an analyses times out in 30 seconds. If you find it's too short time to evaluate the analysis, change it to a higher value.

Now press the `Run Analysis` button. If the analysis succeeds, you will be switched to a result tab - you can now browse the resulting graph just as when browsing a data source.

If the evaluation fails, the plugin boxes turn red and an error description is shown when you hover mouse cursor over them. You can then either try to run it again, or to Edit it using the `Edit` button next to the analysis' title.

---
### <a name="plugins"></a>Plugins

---

# Programmer documentation

The Payola application consists of several layers and libraries that are all enclosed within a solution project ```payola```. The following sections will describe structure of the solution, the functionality hidden within the layers and libraries and their relations.

## Solution structure

The solution is defined using the [SBT](https://github.com/harrah/xsbt/wiki/ "SBT") which isn't tightly coupled to any particuale IDE, so you may generate corresponding project files for the most commonly used IDEs (e.g. IntelliJ IDEA, Eclipse). The SBT doesn't introduce any concept that can be directly used as a solution, but it can be emulated using projects and subprojects. In our case, the ```payola``` solution is just a project with no source files. The solution structure is:

- ```payola```
	- [```common```](#common)
	- [```data```](#data)
	- [```domain```](#domain)
	- [```model```](#model)
	- [```project```](#project)
	- [```s2js```](#s2js)
		- [```adapters```](#adapters)
		- [```compiler```](#compiler)
		- [```runtime```](#runtime)
			- [```client```](#runtime-client)
			- [```shared```](#runtime-shared)
	- [```scala2json```](#scala2json)
	- [```web```](#web)
		- [```client```](#client)
		- [```initializer```](#initializer)
		- [```shared```](#shared)
		- [```server```](#server)

Briefly, the project [```payola/project```](#project) defines that structure, dependencies among the projects, external dependencies and the build process, so it can be understood as a Makefile. Somehow standalone libraries are the [```payola/scala2json```](#scala2json) which provides means of scala object serialization into the JSON format and [```payola/s2js```](#s2js) which with all its subprojects enables us to write web applications in Scala (compile Scala code to equivalent JavaScript code).

The Payola application itself is spread within the rest of the projects, namely [```payola/common```](#common) that defines classes that are used throughout all layers and even on the client side. The [```payola/domain```](#domain) mostly extends classes from the [```payola/common```](#common) with backend logic. The [```payola/data```](#data) is a persistance, data access layer. The [```payola\model``` wraps up the previous three modules with an uniform interface. It's ment as a standard programmatical access point to the Payola. Finally, the web application consists of the [```payola\web\initializer```](#initializer) which is a console application initializing the databases (i.e an installer), [```payola\web\server```](#server) that is a [Play](http://www.playframework.org/) web application and the [```payola\web\client```](#client) which contains a browser MVP application (compiled to JavaScript). Last but not least is the [```payola/web/shared```](#shared) with objects that are called from the client, but executed on the server.

This structure also determines package names, which follow the pattern ```cz.payola.[project path where '/' is replaced with '.']```. So for example a class declared in the ```payola/s2js/compiler``` project can be found in the ```cz.payola.s2js.compiler``` package or one of its subpackages.

<a name="project"></a>
## Project payola/project

<a name="scala2json"></a>
## Package cz.payola.scala2json

<a name="s2js"></a>
## Project payola/s2js

<a name="compiler"></a>
### Package cz.payola.s2js.compiler

<a name="adapters"></a>
### Package cz.payola.s2js.adapters

#### Package cz.payola.s2js.adapters.browser

#### Package cz.payola.s2js.adapters.dom

#### Package cz.payola.s2js.adapters.events

#### Package cz.payola.s2js.adapters.html

#### Package cz.payola.s2js.adapters.js

<a name="runtime"></a>
### Project payola/s2js/runtime

<a name="runtime-client"></a>
#### Package cz.payola.s2js.runtime.client

##### Package cz.payola.s2js.runtime.client.js

##### Package cz.payola.s2js.runtime.client.rpc

##### Package cz.payola.s2js.runtime.client.scala

<a name="runtime-shared"></a>
#### Package cz.payola.s2js.runtime.shared

<a name="common"></a>
## Package cz.payola.common

### Package cz.payola.common.entities

#### Package cz.payola.common.entities.analyses

#### Package cz.payola.common.entities.plugins

#### Package cz.payola.common.entities.privileges

#### Package cz.payola.common.entities.settings

### Package cz.payola.common.rdf

<a name="domain"></a>
## Package cz.payola.domain

<a name="data"></a>
## Package cz.payola.data

### Package cz.payola.squeryl

#### Package cz.payola.squeryl.entities

#### Package cz.payola.squeryl.repositories

### Package cz.payola.virtuoso

<a name="model"></a>
## Package cz.payola.model

<a name="web"></a>
## Package cz.payola.web

<a name="initializer"></a>
### Package cz.payola.web.initializer

<a name="shared"></a>
### Package cz.payola.web.shared

<a name="server"></a>
### Package cz.payola.web.server

<a name="client"></a>
### Package cz.payola.web.client

#### Package cz.payola.web.client.events

#### Package cz.payola.web.client.models

#### Package cz.payola.web.client.views

#### Package cz.payola.web.client.presenters