Burn Up Tool for Trello Boards
==============================

[![Build Status](https://travis-ci.org/arnbak/trello-burnup.svg?branch=master)](https://travis-ci.org/arnbak/trello-burnup)

This tool gives the possibility to create burnup graphs for Trello boards. 

It works by running a daily job, that sum the various points to be able to show the graph that represents the progress.


Usage
-----

Boards should ofcourse be created in Trello. Further more a configuration card should be added to each board for which a graph is needed. 

The configuration card should contain two elements, which defines a start date and an end date. 

1. add a card named ```Config```

2. add two elements ```[start] ddMMyyyy``` and ```[end] ddMMyyy``` which defines the period for which the product is being developed.


To define a scope, add a estimate to each element in each card. It should be prepended to each element in the form: ```[10] some name```

This will add the element in the scope count. A label of the color red or purple can be added to, to remove it from the scope count. 

When a given element is being worked on, a blue label can be added to provide a in progress graph. 

When a task is done, replace the in progress label with a green label. And it will count in the done task. 

The application can be accessed at:

```
http://arnbak.com:9990
```


Developing
----------

Technically the frontend is written in AngularJS, with Bootstrap and to some degree play html templates. The backend is a scala based play framework application with a postgresql database. 

The application is deployed at heroku. And can be accessed at the previously given url.

All the code is in the GIT repository found here: 

```
https://github.com/arnbak/trello-burnup
```

####Getting started

First of all, this is a play framework application. More info can be found here: http://www.playframework.com

Start by cloning the repository

```
$ git clone git@github.com:arnbak/trello-burnup.git
```

Before running the backend part. Setup a database, and add the correct url in the ```conf/application.conf```

Go into the play/activator console by typing 

```$ activator ```

Which gives the following output:

```
[info] Loading project definition from /Users/lars/Development/burnup-app/project
[info] Set current project to burnup-app (in build file:/Users/lars/Development/burnup-app/)
[burnup-app] $
```

Start the app with the ~run command:

```
[burnup-app] $ ~run
```