(ns holyjak.fulcro-exercises
  "This is a series of exercises to help you learn Fulcro, via the REPL. See the README.md!

  How to use:
  - Load this file into the cljs REPL (using the commands provided by your editor)
  - For each exercise:
    1. Un-comment *temporarily* the exercise you are working on (by replacing `(comment ...`
      with `(do ;comment ...`) Shadow-cljs will thus evaluate the code and it will render.
      Tip: Hard-reload the page occasionally to get rid of residues of past exercises.
    2. Read the instructions in the comments, make your changes, wait for shadow to apply them
    3. Regularly look at what your solution looks like in Fulcro Inspect (Client DB, ...) to
       get familiar with this essential tool.
    4. Call `(hint <exercises number>)` to get help. Repeated calls may provide more help.
       Even if you want to do it without help, check out all the hints eventually. They may
       contain useful insights.
    5. When done, compare your solution with mine in the `holyjak.solutions` namespace.
    6. Finally, comment-out the exercise again and go on to the next one.
  - See 'Troubleshooting and getting help during the exercises' in the README
  "
  (:require
    [holyjak.fulcro-exercises.impl :refer [hint config-and-render! show-client-db]]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as norm]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.mutations :refer [defmutation]]
    [com.fulcrologic.fulcro.dom :as dom :refer [button div form h1 h2 h3 input label li ol p ul]]
    [com.wsscode.pathom.connect :as pc :refer [defresolver]]))

(defn init [])

;; ### TODO: Read the namespace docstring for instructions how to work with the exercises ###

(do; comment
  (do
    ;; TASK 0.0: Comment this out by replacing the `(do; comment` above with `(comment` and go on to the next exercise.
    ;; LEARNING OBJECTIVES: Get familiar with switching from an exercise to another.
    (defsc Root00 [_ _]
      (div
        (h1 "Welcome to Fulcro exercises!")
        (p "This is an example what an exercise looks like while you work on it.")
        (p "Just comment this exercises out as describe in the code comment above and go on to the next.")))

    (config-and-render! Root00)
    ,))

(comment ; 0 "Try it out!"
  (do
    ;; LEARNING OBJECTIVES: Get familiar with switching to a new exercise and using the hints.
    (defsc Root0 [_ _]
      (h1 "Hello, I am a Fulcro app from the exercise 0!"))

    (config-and-render! Root0)

    (comment ; try running the hint fn 3 times!
      (hint 0)
      (hint 0)
      (hint 0))

    nil))

(comment ; 1 "Hard-coded DOM"
  (do
    ;; TASK:
    ;; Complete the body of the Root1 component to render the following html:
    ;;
    ;; <div>
    ;;   <h1 id="title" style="text-align: center;">Fulcro is:</h1>
    ;;   <ul>
    ;;     <li>Malleable</li>
    ;;     <li>Full-stack</li>
    ;;     <li>Well-designed</li>
    ;;   </ul>
    ;; </div>
    ;;
    ;; LEARNING OBJECTIVES: Get familiar with creating elements and passing in props.
    ;;
    ;; RESOURCES:
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/#_rendering_dom_the_body_of_a_component
    ;; - https://reactjs.org/docs/dom-elements.html#style
    (defsc Root1 [_ _]
      {}
      "TODO")

    (config-and-render! Root1)))

(comment ; 2 "Extracting a child component"
  (do
    ;; TASK:
    ;; Refactor the previous solution by moving the `<li>` into a separate
    ;; component called `ValuePropositionPoint` and `map` over the provided
    ;; `value-proposition-points` to produce the whole list.
    ;;
    ;; LEARNING OBJECTIVES:
    ;; - Creating and using Fulcro components
    ;; - Creating elements based on data
    ;;
    ;; RESOURCES:
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/#_the_anatomy_of_a_fulcro_component_query_ident_body
    (def value-proposition-points
      [{:proposition/label "Malleable"} {:proposition/label "Full-stack"} {:proposition/label "Well-designed"}])

    (defsc Root2 [_ _]
      {}
      "TODO")

    (config-and-render! Root2)
    ; (hint 2)

    ;; Task 2.b: Make sure you do not get the React error <<Each child in a list
    ;; should have a unique "key" prop.>> in the Chrome Console.
    ; (hint 2)
    ,))

(comment ; 3 "Externalizing data"
  (do
    ;; TASK:
    ;; We still want to render the same HTML but this time we want to read the
    ;; data from Fulcro's client DB, where it is inserted by `config-and-render!`.
    ;; Start from the previous solution but replace the hardcoded text and global
    ;; def with props.
    ;;
    ;; LEARNING OBJECTIVES:
    ;; - Use Fulcro state management as it is meant to be used, reading from the Client DB
    ;;
    ;; RESOURCES:
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/#_the_anatomy_of_a_fulcro_component_query_ident_body
    (defsc Root3 [_ _]
      {}
      "TODO")

    (config-and-render!
      Root3
      {:initial-db
       ;; NOTE: Normally the initial-db we pass here should be already normalized but
       ;; since we do not care about normalization and are happy with denormalized data
       ;; in this exercise, it is OK to pass in the data tree as-is.
       {:page/heading "<3> Fulcro is:"
        :page/value-proposition-points
                      [{:proposition/label "Malleable"}
                       {:proposition/label "Full-stack"}
                       {:proposition/label "Well-designed"}]}})

    ;(hint 3)
    ;; Tip: Use Fulcro Inspect to see the content of the client DB
    ;;      Also try to use the provided `(show-client-db)` function for that.
    ,))

(comment ; 4 "Insert data into the client DB with merge/merge!"
  (do
    ;; TASK:
    ;; Again we build on the previous solution but instead of inserting the data
    ;; at app initialization, we will revert to the render without :initial-db and insert them
    ;; manually with a call to `merge/merge!`
    ;;
    ;; LEARNING OBJECTIVES:
    ;; - Reason about data ingestion and representation
    ;; - Use Fulcro Inspect
    ;; - Understand merge! as a crucial step on the path to load!
    ;;
    ;; RESOURCES:
    ;; - the intro to merge-component! (https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/#_pre_study_merging_data_into_the_client_db_with_merge_component)
    ;;   is relevant because merge! behaves very similary to merge-component! - the difference is that it works for the Root instead of
    ;;   some child component and it takes a query instead of a component (because it doesn't need an ident)
    ;; - merge! docs: https://book.fulcrologic.com/#_using_com_fulcrologic_fulcro_componentsmerge
    (defsc Root4 [_ _]
      {}
      "TODO")

    (def app4 (config-and-render! Root4))

    ;; What do you think the client DB will look like? Think, write it down, then check it
    ;; using Fulcro Inspect - DB (or `(show-client-db)`)
    (merge/merge! app4 nil nil) ; TODO Implement
    ; (hint 4)
    ; (hint 4)
    ,))

(comment ; 5 "Normalization and merge-component!"
  (do
    ;; TASK:
    ;; Let's see how to insert data into the client DB for our UI to display!
    ;; We have this UI:
    ;; Root1
    ;;   Team(s)
    ;;     Player(s)
    ;;       Address
    ;; and a tree of data to give to it. There is now a few tasks awaiting you,
    ;; described in comments inside the code below. So read on!
    ;;
    ;; BEWARE: The provided code on purpose omits some things you would normally have included in your components
    ;;
    ;; LEARNING OBJECTIVES:
    ;; - Understand idents and their use by Fulcro
    ;; - Reason about data normalization
    ;; - Understand merge-component! as another crucial step on the path to load!
    ;;
    ;; RESOURCES:
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/#_components_query
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/#_components_ident
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/#_pre_study_merging_data_into_the_client_db_with_merge_component
    ;; - https://book.fulcrologic.com/#_using_mergemerge_component
    (defsc Address [_ {city :address/city}]
      {:query [:address/city]}
      (p "City: " city))

    (defsc Player [_ {:player/keys [name address]}]
      {:query [:player/id :player/name :player/address]}
      (li "name: " name " lives at: " ((comp/factory Address) address)))

    (def ui-player (comp/factory Player {:keyfn :player/id}))

    (defsc Team [_ {:team/keys [name players]}]
      {:query [:team/id :team/name :team/players]}
      (div (h2 "Team " name ":")
           (ol (map ui-player players))))

    (def ui-team (comp/factory Team {:keyfn :team/id}))

    (defsc Root5 [_ {teams :teams}]
      {:query [:teams]} ; NOTE: This is on purpose incomplete
      (div
        (h1 "Teams")
        (p "Debug: teams = " (dom/code (pr-str teams)))
        (map ui-team teams)))

    (def data-tree
      "The data that our UI should display"
      {:teams [#:team{:name "Hikers" :id :hikers
                      :players [#:player{:name "Jon" :address #:address{:city "Oslo"} :id 1}
                                #:player{:name "Ola" :address #:address{:city "Trondheim"} :id 2}]}]})

    ;; Render the app (without any data so far):
    (def app5 (config-and-render! Root5))

    ;; Now:
    ;; 1. Uncomment, complete, and run the merge/merge! call below to insert the data-tree into
    ;;    the client DB. Check the UI shows it.
    (comment (merge/merge! ...))
    ; (hint 5)
    ; (hint 5)

    ;; More tasks!
    ;; 2. Make sure the data is normalized in the DB by adding idents. What will the DB look like?
    ; (hint 5)
    ; (hint 5)
    ;;
    ;; 3. Now, even if your idents are correct, the data still does not
    ;;    seem to be normalized as you would expect. Why? What did we forget?
    ;;    Is something wrong with our queries?! (Hint: yes, it is)
    ;;    Fix it and try again!
    ;;    Tip: force-reload the page to clean the client DB
    ; (hint 5)

    ;; 4. Rewrite the code to use merge/merge-component! instead.
    ;;    What is the correct target component? How to make sure the data looks
    ;;    just like before?
    ;;   Really try your best to make the right merge-component! call!
    ; (hint 5)
    ; (hint 5)
    ; (hint 5)
    ,))

(comment ; 6 Client-side mutations
  (do
    ;; TASK:
    ;; Enable the user to select individual elements or all at once
    ;; and delete these from the Client DB. (No remote in this exercises.)
    ;;
    ;; Your task is thus to implement the mutations `set-players-checked` and `delete-selected`,
    ;; and trigger them in the :onClick handlers below instead of the current `println`.
    ;; Keep to the suggested inputs of those mutations, to make comparison easier.
    ;;
    ;; (Note: We could have structured the mutations in a simpler way. But this one
    ;; provides you a sufficient challenge.)
    ;;
    ;; Tips:
    ;; - It is often useful to put most mutation logic into a pure helper fn state-map -> state-map
    ;;   (often with the same name but ending with *)
    ;; - Get the current state-map via `(app/current-state app6)` to play with your code in the REPL
    ;; - Use Fulcro Dev Tools to look at the database and Transactions
    ;; - Check `(hint 6)` is you need help
    ;;
    ;; LEARNING OBJECTIVES:
    ;; - React to user actions
    ;; - Create and trigger a mutation
    ;; - Use and manipulate the data in the Client DB
    ;; - Understand the need for UI-only (`:ui/*`) props
    ;; - Use Fulcro Inspect to explore the client DB and to troubleshoot transactions/mutations
    ;;
    ;; RESOURCES:
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/#_changing_global_data_and_performing_remote_calls_mutations
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/#def-clientdb
    ;; - Fulcro Inspect and its parts: https://blog.jakubholy.net/2020/troubleshooting-fulcro/#_know_your_tools

    (defn make-player->team
      "A helper function to create a 'lookup' so that we can easily find a player's team.
      You might - or not :-) - find it useful in your mutations.

      Take a seq of teams and look into their `:team/players` to construct the map
      `player-id` -> `team-id`, useful to look up a player's team."
      ;; To try it out:
      ;; `(->> (app/current-state app6) :team/id vals make-player->team)`
      [teams]
      (into {}
            (for [{team-id :team/id
                   players :team/players} teams
                  [_ player-id]           players]
              [player-id team-id])))

    (defsc Player [this {:keys [player/id player/name ui/checked?]}]
      {:query [:player/id :player/name :ui/checked?]
       :ident :player/id}
      (li
        (input {:type    "checkbox"
                :checked (boolean checked?)
                :onClick #(println "TODO: trigger the mutation `(set-players-checked {:players [id] :value (not checked?)})`")})
        name))

    (def ui-player (comp/factory Player {:keyfn :player/id}))

    (defsc Team [this {:team/keys [name players] checked? :ui/checked?}]
      {:query [:team/id :team/name :ui/checked? {:team/players (comp/get-query Player)}]
       :ident :team/id}
      (let [all-checked? (and (seq players) (every? :ui/checked? players))]
        (div (h2 "Team " name ":")
             (label (input {:type    "checkbox"
                            :checked all-checked?
                            :onClick #(println "TODO: trigger the mutation `(set-players-checked {:players (map :player/id players) :value   (not all-checked?)})`")})
                    "Select all")
             (ol (map ui-player players)))))

    (def ui-team (comp/factory Team {:keyfn :team/id}))

    (defsc Root6 [this {teams :teams}]
      {:query [{:teams (comp/get-query Team)}]}
      (form
        (h1 "Teams")
        (button {:type "button"
                 :onClick #(println "TODO: trigger the mutation `(delete-selected nil)`")}  ; TODO implement
                "Delete selected")
        (map ui-team teams)))

    (def app6 (config-and-render! Root6))

    (run!
      #(merge/merge-component! app6 Team % :append [:teams])
      [#:team{:name "Explorers" :id :explorers
              :players [#:player{:id 1 :name "Jo"}
                        #:player{:id 2 :name "Ola"}
                        #:player{:id 3 :name "Anne"}]}
       #:team{:name "Bikers" :id :bikers
              :players [#:player{:id 4 :name "Cyclotron"}]}])

    ,))

(comment ; 7 load!-ing data from a remote
  (do
    ;; TASK:
    ;; Learn how to load! data and practice using Fulcro Inspect
    ;; This is similar to #5 but with merge-component! replaced with load!
    ;; We now run a mock, in-browser server (with a real Pathom).
    ;; Read on to find the task your should do.
    ;;
    ;; LEARNING OBJECTIVES:
    ;; - Use load!, with targeting
    ;; - Create Pathom resolvers
    ;; - Use the EQL and Network tabs of Fulcro Inspect
    ;; - Use load markers to track the state of data loading
    ;;
    ;; RESOURCES:
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/#_loading_remote_data
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/#_targeting_adding_references_to_the_new_data_to_existing_entities
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/#_how_to
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/#_when_to_load
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/#_bonus_tracking_loading_state_with_load_markers

    ;; --- "Frontend" UI ---
    (defsc Address [_ {city :address/city}]
      {:query [:address/city]
       :ident :address/city}
      (p "City: " city))

    (defsc Player [_ {:player/keys [name address]}]
      {:query [:player/id :player/name {:player/address (comp/get-query Address)}]
       :ident :player/id}
      (li "Player: " name " lives at: " ((comp/factory Address) address)))

    (def ui-player (comp/factory Player {:keyfn :player/id}))

    (defsc Team [_ {:team/keys [name players]}]
      {:query [:team/id :team/name {:team/players (comp/get-query Player)}]
       :ident :team/id}
      (div (h2 "Team " name ":")
           (ol (map ui-player players))))

    (def ui-team (comp/factory Team {:keyfn :team/id}))

    (defsc Root7 [this {teams :teams :as props}]
      {:query [{:teams (comp/get-query Team)}]}
      (div
        ;; Code for task 2 (described further down) - un-comment and complete this code:
        ;(button {:type "button"
        ;         :onClick #(println "df/load! the data from here")} "Load data")
        (let [loading? false] ; scaffolding for TASK 5
          (cond
            loading? (p "Loading...")
            ;; ...
            :else
            (comp/fragment (h1 "Teams")
                           (map ui-team teams))))))

    ;; --- "Backend" resolvers to feed data to load! ---
    (defresolver my-very-awesome-teams [_ _] ; a global resolver
      {::pc/input  #{}
       ::pc/output [{:teams [:team/id :team/name
                             {:team/players [:player/id :player/name 
                                             ;; NOTE: We need this 👇 instead of just `:player/address` so that autocomplete
                                             ;; in Fulcro Inspect - EQL understands this is address and can get to id, city
                                             {:player/address [:address/id]}]}]}]}
      {:teams [#:team{:name "Hikers" :id :hikers
                      :players [#:player{:id 1 :name "Luna" :address {:address/id 1}}
                                #:player{:id 2 :name "Sol" :address {:address/id 2}}]}]})

    (defresolver address [_ {id :address/id}] ; an ident resolver
      {::pc/input #{:address/id}
       ::pc/output [:address/id :address/city]}
      (case id
        1 #:address{:id 1 :city "Oslo"}
        2 #:address{:id 2 :city "Trondheim"}))

    ;; Render the app, with a backend using these resolvers
    (def app7 (config-and-render! Root7 {:resolvers [address my-very-awesome-teams]}))

    ;; TODO: TASK 1 - use `df/load!` to load data from the my-very-awesome-teams
    (println "TODO: df/load! should be invoked here...")
    ;; (Remember `(hint 7)` when in need.)
    ;; Now check Fulcro Inspect - the Transactions and Network tabs and explore the load there.
    ;; In both, click on the corresponding line to display details below. In the load's details
    ;; in the Network tab, press the [Send to query] button to show it in the EQL tab.
    ;; Run it from the EQL tab. Modify, run again.
    ;; - EQL tab - do [(Re)load Pathom Index] to get auto-completion for the queries and try to type some
    ;; - Index Explorer tab - do [Load index], explore the index (you might need to scroll up on the right side to see the selected thing)

    ;; TODO: TASK 2 - replace loading data during initialization (above) with loading them on-demand, on the button click

    ;; TODO: TASK 3 - split ident resolvers for a team and a player out of `my-very-awesome-teams`, as we did for address;
    ;;       Then play with them using Fulcro Inspect's EQL tab - fetch just the name of a particular person; ask for
    ;;       a property that does not exist (and check both the EQL tab and the Inspect's Network tab) - what does it look like?

    ;; TODO: TASK 4 - use targeting to fix a mismatch between a resolver and the UI: in `Root7`, rename `:teams` to `:all-teams`; how
    ;;       do you need to change the load! for this to work as before?
    ;;       Check in the Client DB that the changed data look as expected.

    ;; TODO: TASK 5 - Use Fulcro load markers to display "Loading..." instead of the content while loading the data (see Root7)

    ,))

;; TODO Additional exercises:
;; - computed props for passing a callback from the parent
;; - create/delete/create+delete <> tmpids; simulated failure => undo the optimistic change?
;; - link queries & more from https://blog.jakubholy.net/2020/fulcro-divergent-ui-data/
;; - anything else from the Minim. Fulcro Tutorial should be added?
;;   - Initial state propagation?
;;   - Computed props for a callback or parent-visible prop
;;   - pre-merge ?! / loading dyn. data for a defsc containing a router x we did not learn routers
;;   - Link Query? But not covered by MFT
;; Other:
;; @peterdee:  incremental loading of big trees of data. Incremental loading is probably discussed 
;; adequately in the Developer Guide, but not with recursive queries, and incremental expansion of a UI tree, 
;; I think. If that seems like too much an edge case, maybe something simpler with trees.
