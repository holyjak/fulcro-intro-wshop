Workshop: Introduction to Fulcro
================================

An online workshop held as a part of [re:Clojure 2021](https://www.reclojure.org/) on December 1st, 3-5pm GMT. See the [workshop Eventbrite page](https://www.eventbrite.com/e/reclojure-introduction-to-fulcro-workshop-tickets-188718210247) for details and registration.

*Get your hands dirty with Fulcro to experience and "get" this unique, full-stack web framework for Clojure*

Fulcro is unique among Clojure web frameworks in providing a complete, integrated, full-stack solution for creating non-trivial web applications. It is based on a few simple ideas with far-reaching consequences, it is unusually malleable, and we love it for its focus on creating maintainable, developer-friendly code.

In this workshop you will get a brief introduction to Fulcro and then get your hands dirty exploring the concepts in practice on an existing application in a series of guided exercises. We will use the excellent Fulcro Inspect tooling and mess up with the code.

Prerequisites
--------------

* Clone / download this repository to a directory on your computer
* Install Java, [Clojure CLI tools](https://clojure.org/guides/getting_started), [Node and npm](https://nodejs.org/en/)
* In a Chromium browser such as Vivaldi or Chrome:
  * Install [Fulcro Inspect](https://chrome.google.com/webstore/detail/fulcro-inspect/meeijplnfjcihnhkpanepcaffklobaal)
  * Install [React Developer Tools](https://chrome.google.com/webstore/detail/fulcro-inspect/meeijplnfjcihnhkpanepcaffklobaal)
  * Configure [Chrome Development Settings](https://developers.google.com/web/tools/chrome-devtools/customize): 
    * Under "Console": "Enable Custom Formatters"
    * Under "Network": "Disable Cache (while devtools is open)"

After that, **run the application** as described below at least once, so that all dependencies are downloaded before the workshop.

Usage
-----

```bash
yarn install
npx shadow-cljs watch todomvc
# In another terminal:
❯ clj -A:dev
Clojure 1.10.3
user=> ((requiring-resolve 'fulcro-todomvc.server/http-server))
```

then go to http://localhost:8181/index.html