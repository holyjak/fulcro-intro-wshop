# A very brief introduction to Fulcro

## Workshop agenda

1. Intro (here)
2. Workshop exercises (1-9)
   * We might not have time for them all - you can finish on your own
   * Review [Summary - lessons learned](Workshop.adoc#summary---lessons-learned) at the end
3. Q&A session
4. (Coding exercises - likely a home work, unless we are really quick)
   * Learn building a Fulcro app from "scratch," once concept at a time

## Fulcro intro

_Disclaimer: I'm not going to talk much about WHY things are they way they are. Read https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/index.html#_why_fulcro to learn about that._

### Why Fulcro?

Because it provides a complete, integrated model and solution for building non-trivial business applications, applications that span front- and backend as they essentially all do.
Being integrated, it substantially reduces the amount of boilerplate you have to write, and being "complete," it often provides all you need, without having to add other libraries or make ad-hoc solutions.

* (Really) Data-driven, UI = f(data)
* Sustainable webapp development - minimize incidental complexity
* Dev friendliness - co-location, tooling, navigability => no more editing N disparate places, searching for string IDs!
* Extreme customizability
* Graph API >> REST (see Wilker's talk)

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

### Keyword cloud

* Component
* Component tree
* Data tree
* Mutation
* Transaction
* Client DB
* EQL
* Query

TP: You do not need to understand everything here. We will be coming back to these terms in the exercises. There you will experience and grok them.

## Additional resources

* [Data Navigation with Pathom 3](https://www.youtube.com/watch?v=YaHiff2vZ_o) by Wilker Lucio is a great explanation of the problem with REST and multiple clients (e.g. UI components) with varying data needs and why an attribute-centric approach - such as implemented by Pathom - is a better solution. You will see Pathom in action and learn about some of its super-powers. all of this in just 45 minutes.