(ns holyjak.fulcro-exercises.impl
  (:require
    [holyjak.fulcro-exercises.mock-server :refer [mock-remote]]
    [clojure.string :as str]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]))

(declare hints)
(defonce hints-shown (atom nil))

(defn hint [exercise-nr]
  {:pre [(int? exercise-nr)
         (not (neg? exercise-nr))]}
  (let [exercise-hints   (get hints exercise-nr)
        shown-previously (get @hints-shown exercise-nr 0)
        next?            (< shown-previously (count exercise-hints))
        more?            (not= (inc shown-previously) (count exercise-hints))
        printit!         #(println (str/join "\n\n" %))]
    (cond
      (nil? exercise-hints) "Sorry, no hints available for this exercise"

      next?
      (do (swap! hints-shown update exercise-nr (fnil inc 0))
          (cond-> (subvec exercise-hints shown-previously (inc shown-previously))
                  more? (conj "Repeat for more...")
                  true printit!))

      :else
      (printit! exercise-hints))))

(defonce current-app (atom nil))

(defn show-client-db
  "Print the current content of Fulcro's Client DB, if any."
  []
  (some-> @current-app
          ::app/state-atom
          deref
          cljs.pprint/pprint))

(defn config-and-render!
  "Renders the given root component, also creating a new Fulcro app for it.

  Args:
  - `RootComponent` - the class of the component to render
  - `opts` - options, including:
    - `:resolvers` - a sequence of Pathom resolvers; if provided, a Pathom 'backend'
       (though running in the browser) with these resolvers will be set up
    - `:initial-db` - the initial value of the client DB (normalized)

  Return the new Fulcro app."
  ([RootComponent] (config-and-render! RootComponent nil))
  ([RootComponent {:keys [initial-db resolvers]}]
   (let [current-root? (= (some-> @current-app app/root-class .-name)
                          (.-name RootComponent))
         remotes       (some-> resolvers seq mock-remote)
         app           (if current-root? @current-app (app/fulcro-app
                                                        {:initial-db initial-db
                                                         :remotes    remotes}))]

     (when remotes
       (println "LOG: Configured the remote" (-> remotes keys first) "for the Fulcro App"))
     (reset! current-app app)
     (println "LOG: Rendering" RootComponent "...")
     (app/mount! app RootComponent "app" {:initialize-state? (some? initial-db)})
     app)))

(defn refresh []
  (when-let [app @current-app]
    (app/mount! app
                (app/root-class app)
                "app")))

(comment
  (hint 4)
  (comp/component-name (app/root-class @current-app)),)

(def hints
  {0 ["Awesome, I see you got the hang of it!"
      "No more hints here, sorry!"]
   2 ["Create defsc ValuePropositionPoint, remember to use comp/factory. You do not need any query, just use the props the parent passes in."
      "2.b Look at the options that comp/factory takes. Remember you can use :proposition/label as a function."]
   3 ["Remember to make a join with comp/get-query to include the child's (i.e. ValuePropositionPoint's) query in Root"]
   4 ["Simply pass in to `merge!` the same data-tree that we passed as initial-db to `config-and-render!` in the previous exercise."
      "`merge!` also needs a query; and since we are passing all the app data, it should be the root query"]
   5 ["5.1a You want to use the query of the root component, Root5"
      "5.1b Use `comp/get-query` to get it!"
      "5.2a Normalization: Add idents to all components (but the root). Use the https://book.fulcrologic.com/#_keyword_idents form."
      "5.2b Normalization: Use :team/id, :player/id, :address/city as the idents (same as `(fn [] [:team/id (:team/id <props>)])` ...). But also remember to add the IDs to the queries!"
      "5.3 Normalization fix: remember to use comp/get-query in your queries!"
      "5.4a merge-component!: We cannot use Root5 because it has no ident and thus we cannot merge the whole tree. But we can merge a team's data using the correct component."
      "5.4b Inserting the team's data is not enough - we also need to re-establish the 'edge' between it and `:teams`. Look at the client DB and see it is missing! Look at what options merge-component! takes to support this."
      "5.4c Add the arguments `:append [:teams]`"]
   6 ["You only need the `(action [{:keys [state]}] ...`) part of the mutation (where `state` is an atom containing the state-map a.k.a. client DB)"
      "Remember that you must not only remove the player her/himself but also any reference to her/him from a team's players list. `merge/remove-ident*` will help with that."]
   7 ["7.1a The `server-property` passed to load should be a property a global resolver returns. In this case `:teams`."
      "7.1b The component class passed to load! holds the query defining what to fetch for each element. Since we are getting a list of teams, the component should be `Team`"
      "7.2 load! does internally transact a mutation and can be called as-is, directly from the :onClick. Use the component's `this` instead of `app7` there."
      "7.3 After you factor out a resolver, remember to add it to the `:resolvers` list, as Pathom needs to be informed of it."
      "7.4 Use the `:target` option of load! with `(targeting/replace-at ..)`."]})

