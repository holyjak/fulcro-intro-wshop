# A very brief introduction to Fulcro for the general public

_Questions to the audience: How many know React? GraphQL? Redux? Web dev? Heard of Clojure?_

The purpose of this workshop is to bring the lessons from the Clojure web framework Fulcro to the general public. You are most likely never going to use Fulcro itself but you can still seek or apply its ideas elsewhere.

## What is so great about Fulcro?

In one word: **productivity**

First a personal story. My love affair with Fulcro started with suffering. I worked with a React & Redux FE app and was horrified at the amount of work, boilerplate code, and number of changed files it took to get a piece of data from the BE onto the screen. Then I discovered Fulcro. There it takes 2-3 changes: write the new UI component, include it in a parent component, and possibly add a backend function to fetch the data.

How is this possible?

Thanks to:

1. focus on _sustainable webapp development_ (`explain!`), which leads to 
  * _locality_ of code 
  * _navigability_ of code
  * being data-driven and REPL-able
  * awesome dev tooling
  * (and generally to eliminating accidental complexity and boilerplate)
2. not shying away from the questions _how do I fetch data?_ and _how do I change it?_ and providing a full-stack, \*integrated answer\* (many UI frameworks pretend it is not their problem)
3. leveraging a Graph API instead of REST (since UI/props is a tree, using a Graph API makes perfect sense)
4. <del>(Being conservative about features baked into the framework and providing numerous extension points to customize it - important but not discussed in this workshop)</del>

# World's shortest introduction to Clojure syntax

You will be looking at some Clojure code and data so let's spend few minutes learning to understand the syntax. Fortunately it is very simple!

[Open PDF](./Clojure-syntax-intro-slides.pdf)

----

# Content copied from INTRO.md

### What is Fulcro?

Fulcro is a full-stack web framework for a _graph API_. Let's unpack that:

* Full-stack = both _frontend_ and _backend_
* On the _frontend_ we have a **UI** tree composed of React _components_
* It talks to a **graph API** on the _backend_ to query for data and to effect _mutations_ (changes)
  * A single endpoint (x REST)
  * _Frontend describes_ what data it wants using EDN Query Language (EQL) and _backend fills them in_, returning a data tree
* On the _backend_ we have _Pathom_ - Fulcro's twin library (cljc) that _parses_ EQL and answers with data
  * You code _resolvers_ that provide parts of the answer
  * Your Pathom resolvers typically talk to a DB such as Datomic and PostgreSQL or remote REST APIs
  * Note: Pathom can also run in the browser and it can work as an adapter of a REST API, as [demonstrated on the Twitter v1 API](https://youtu.be/YaHiff2vZ_o?t=1210) in Wilker Lucio's talk "Data Navigation with Pathom 3"

![](./images/fulcro-system-view.svg)

Let's zoom in on the Frontend. It has:

* the **UI** component tree
* a client-side **state**, called _client DB_, and storing data mostly in a _normalized_ form
* an asynchronous **transaction** subsystem (_Tx_) for triggering local and remote mutations and data loads from the components

Let's zoom in even more, on the UI tree and its rendering:

![](./images/fulcro-ui-query-data.svg)

1. Fulcro asks the `Root` component for its _query_, which composes the queries of its children (and so forth)
2. Fulcro fulfills the query using the data in the client DB, producing a tree of data, also known as _props_ (= properties)
3. Fulcro invokes the Root's render function passing it the props tree; Root in turn renders its children, passing them the relevant sub-trees

### Fulcro component

What is a Fulcro component anyway?

```clojure
(defsc <Name> [this props]
  {<config options>} ; :query, :ident, ...
  (dom/div 
    (dom/h1 "Hello" (:name props) "!")
    (some-child-component (:some-child props))))
```

## Additional resources

* [Data Navigation with Pathom 3](https://www.youtube.com/watch?v=YaHiff2vZ_o) by Wilker Lucio is a great explanation of the problem with REST and multiple clients (e.g. UI components) with varying data needs and why an attribute-centric approach - such as implemented by Pathom - is a better solution. You will see Pathom in action and learn about some of its super-powers. all of this in just 45 minutes.

----
