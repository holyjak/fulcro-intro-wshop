(ns holyjak.solutions
  "Solutions to the exercises - check them against your and peek at them if you get stuck."
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
    [com.wsscode.pathom.connect :as pc :refer [defresolver]]
    [com.fulcrologic.fulcro.mutations :as m]))

;; TIP: Use your editor's powers to collapse all the comments and only open the one you want to study
;;      (Cursive: 'Collapse All', `Shift Cmd -` on Mac)

(comment ; 1 "Hard-coded DOM"
  (do
    (defsc Root1 [_ _]
           {}
           #_"TODO"
           (div
             (h1 :#title {:style {:textAlign "center"}} "Fulcro is:")
             (ul
               (li "Malleable")
               (li "Full-stack")
               (li "Well-designed"))))

    (config-and-render! Root1)))








(comment ; 2 "Extracting a child component"
  (do
    (def value-proposition-points
      [{:proposition/label "Malleable"} {:proposition/label "Full-stack"} {:proposition/label "Well-designed"}])

    (defsc ValuePropositionPoint [_ {:proposition/keys [label]}]
           (li label))

    (def ui-value-proposition-point (comp/factory ValuePropositionPoint {:keyfn :proposition/label}))

    (defsc Root2 [_ _]
           {}
           #_"TODO"
           (div
             (h1 :#title {:style {:textAlign "center"}} "<2> Fulcro is:")
             (ul (map ui-value-proposition-point value-proposition-points))))

    (config-and-render! Root2)
    ,))








(comment ; 3 "Externalizing data"
  (do
    (defsc ValuePropositionPoint [_ {:proposition/keys [label]}]
           {:query [:proposition/label]}
           (li label))

    (def ui-value-proposition-point (comp/factory ValuePropositionPoint {:keyfn :proposition/label}))

    (defsc Root3 [_ {:page/keys [heading value-proposition-points]}]
           {:query [:page/heading {:page/value-proposition-points (comp/get-query ValuePropositionPoint)}]}
           #_"TODO"
           (div
             (h1 :#title {:style {:textAlign "center"}} "hdr:" heading)
             (ul (map ui-value-proposition-point value-proposition-points))))

    (config-and-render!
      Root3
      {:initial-db
       {:page/heading "<3> Fulcro is:"
        :page/value-proposition-points
                      [{:proposition/label "Malleable"}
                       {:proposition/label "Full-stack"}
                       {:proposition/label "Well-designed"}]}})
    ,))








(comment ; 4 "Insert data into the client DB with merge/merge!"
  (do
    (defsc ValuePropositionPoint [_ {:proposition/keys [label]}]
           {:query [:proposition/label]}
           (li label))

    (def ui-value-proposition-point (comp/factory ValuePropositionPoint {:keyfn :proposition/label}))

    (defsc Root4 [_ {:page/keys [heading value-proposition-points]}]
           {:query [:page/heading {:page/value-proposition-points (comp/get-query ValuePropositionPoint)}]}
           #_"TODO"
           (div
             (h1 :#title {:style {:textAlign "center"}} "hdr:" heading)
             (ul (map ui-value-proposition-point value-proposition-points))))

    (def app4 (config-and-render! Root4))

    (merge/merge!
      app4
      {:page/heading "<4> Fulcro is:"
       :page/value-proposition-points
                     [{:proposition/label "Malleable"}
                      {:proposition/label "Full-stack"}
                      {:proposition/label "Well-designed"}]}
      (comp/get-query Root4))
    ,))








(comment ; 5 "Normalization and merge-component!"
  ;; VARIANT 5.1
  (do
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

    (merge/merge! app5 data-tree (comp/get-query Root5))
    ,))

(comment ; 5 "Normalization and merge-component!"
  ;; VARIANT 5.2 + 5.3
  (do
    (defsc Address [_ {city :address/city}]
           {:query [:address/city]
            :ident :address/city}
           (p "City: " city))

    (defsc Player [_ {:player/keys [name address]}]
           {:query [:player/id :player/name {:player/address (comp/get-query Address)}]
            :ident :player/id}
           (li "name: " name " lives at: " ((comp/factory Address) address)))

    (def ui-player (comp/factory Player {:keyfn :player/id}))

    (defsc Team [_ {:team/keys [name players]}]
           {:query [:team/id :team/name {:team/players (comp/get-query Player)}]
            :ident :team/id}
           (div (h2 "Team " name ":")
                (ol (map ui-player players))))

    (def ui-team (comp/factory Team {:keyfn :team/id}))

    (defsc Root5 [_ {teams :teams}]
           {:query [{:teams (comp/get-query Team)}]} ; NOTE: This is on purpose incomplete
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

    (merge/merge! app5 data-tree (comp/get-query Root5))
    ,))

(comment ; 5 "Normalization and merge-component!"
  ;; VARIANT 5.4

  ;; !!! BEWARE: Only the merge-component! part provided here,
  ;;     which would replace the original call to merge/merge!;
  ;;     the rest is the same.
  ;; <THE SAME CODE AS IN 5.3 HERE...>
  (merge/merge-component!
    app5
    Team
    (-> data-tree :teams first)
    :append [:teams])
  ,)








(comment ; 6 Client-side mutations
  ;; NOTE: There are many ways to implement the details, this is just one
  (do

    (defn set-player-checked* [new-value state-map id]
      (assoc-in state-map [:player/id id :ui/checked?] new-value))

    (defmutation set-players-checked [{:keys [players value]}]
      (action [{:keys [state]}]
        (swap! state #(reduce (partial set-player-checked* value) % players))))

    (defn make-player->team
      "Take a seq of teams and look into their `:team/players` to construct the map
      `player-id` -> `team-id`, useful to look up a player's team."
      ;; To try it out:
      ;; `(->> (app/current-state app6) :team/id vals make-player->team)`
      [teams]
      (into {}
            (for [{team-id :team/id
                   players :team/players} teams
                  [_ player-id]           players]
              [player-id team-id])))

    ;; OPTION B: DIY deletion:
    ;(defn delete-player [state-map [player-id team-id]]
    ;  (-> state-map
    ;      (update :player/id dissoc player-id)
    ;      (merge/remove-ident* [:player/id player-id]
    ;                           [:team/id team-id :team/players])))

    (defn delete-selected* [{player-map :player/id :as state-map}]
      (let [player->team  (make-player->team (-> state-map :team/id vals))
            selected-player-ids
                          (->> player-map
                               vals
                               (filter :ui/checked?)
                               (map :player/id))
            player-teams (map player->team selected-player-ids)]
        ;; OPTION B: DIY deletion:
        #_(reduce
            delete-player
            state-map
            (map vector selected-player-ids player-teams))
        ;; OPTION A: using built-in helpers
        (reduce
          norm/remove-entity
          state-map
          (map #(vector :player/id %) selected-player-ids))))

    (defmutation delete-selected [_]
      (action [{:keys [state]}]
        (swap! state delete-selected*)))

    (defsc Player [this {:keys [player/id player/name ui/checked?]}]
           {:query [:player/id :player/name :ui/checked?]
            :ident :player/id}
           (li
             (input {:type    "checkbox"
                     :checked (boolean checked?)
                     :onClick #(transact! this [(set-players-checked {:players [id] :value (not checked?)})])})
             name))

    (def ui-player (comp/factory Player {:keyfn :player/id}))

    (defsc Team [this {:team/keys [name players] checked? :ui/checked?}]
           {:query [:team/id :team/name :ui/checked? {:team/players (comp/get-query Player)}]
            :ident :team/id}
           (let [all-checked? (boolean (and (seq players) (every? :ui/checked? players)))]
             (div (h2 "Team " name ":")
                  (label (input {:type    "checkbox"
                                 :checked all-checked?
                                 :onClick #(transact! this [(set-players-checked {:players (map :player/id players)
                                                                                  :value   (not all-checked?)})])})
                         "Select all")
                  (ol (map ui-player players)))))

    (def ui-team (comp/factory Team {:keyfn :team/id}))

    (defsc Root6 [this {teams :teams}]
           {:query [{:teams (comp/get-query Team)}]}
           (form
             (h1 "Teams")
             (button {:type "button"
                      :onClick #(transact! this [(delete-selected nil)])} "Delete selected")
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
    ;; VARIANT 7.1 and 7.2

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

    (defsc Root7 [this {teams :teams}]
      {:query [{:teams (comp/get-query Team)}]}
      (div
        (button {:type "button" ; VARIANT 7.2; comment out for 7.1
                 :onClick #(df/load! this :teams Team)} "Load data")
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
                                        {:team/players [:player/id :player/name {:player/address [:address/id]}]}]}]}
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

    (df/load! app7 :teams Team) ; VARIANT 7.1; comment out for 7.2

    ;; TODO: TASK 3 - split ident resolvers for a team and a player out of my-very-awesome-teams, as we did for address;
    ;;       Then play with them using Fulcro Inspect's EQL tab - fetch a particular person with just the name; ask for
    ;;       a property that does not exist (and check both the EQL tab and the Inspect's Network tab)

    ,))

(comment ; VARIANT 7.3 - separate resolvers
  (do

    ;; BEWARE: Only the resolvers and render presented here, the UI is the same as above.
    ;; <THE SAME FRONTEND CODE AS IN 7.1 + 7.2 HERE...>

    ;; --- "Backend" resolvers to feed data to load! ---
    (defresolver my-very-awesome-teams [_ _] ; a global resolver
      {::pc/input  #{}
       ::pc/output [{:teams [:team/id :team/name :team/players]}]}
      {:teams [#:team{:name "Hikers" :id :hikers
                      :players [#:player{:id 1}
                                #:player{:id 2}]}]})

    (defresolver team [_ {id :team/id}] ; an ident resolver
      {::pc/input #{:team/id}
       ::pc/output [:team/id :team/name :team/players]}
      (case id
        :hikers #:team{:id :hikers :name "Hikers"
                       :players [#:player{:id 1} #:player{:id 2}]}))

    (defresolver player [_ {id :player/id}] ; an ident resolver
      {::pc/input #{:player/id}
       ::pc/output [:player/id :player/name {:player/address [:address/id]}]}
      (case id
        1 #:player{:id 1 :name "Luna" :address {:address/id 1}}
        2 #:player{:id 2 :name "Sol" :address  {:address/id 2}}))

    (defresolver address [_ {id :address/id}] ; an ident resolver
      {::pc/input #{:address/id}
       ::pc/output [:address/id :address/city]}
      (case id
        1 #:address{:id 1 :city "Oslo"}
        2 #:address{:id 2 :city "Trondheim"}))

    ;; Render the app, with a backend using these resolvers
    (def app7 (config-and-render! Root7 {:resolvers [address my-very-awesome-teams player team]}))

    ,))

(comment ; VARIANT 7.4 - targeting
  (do
    ;; BEWARE: Only the changed parts presented here, the rest is the same as above.
    ;; <THE SAME CODE AS ABOVE EXCLUDING Root7 HERE...>
    (defsc Root7 [this {teams :all-teams}]
      {:query [{:all-teams (comp/get-query Team)}]}
      (div
        (button {:type "button"
                 :onClick #(df/load! this :teams Team {:target (targeting/replace-at [:all-teams])})} "Load data")
        (let [loading? false] ; scaffolding for TASK 5
          (cond
            loading? (p "Loading...")
            ;; ...
            :else
            (comp/fragment (h1 "Teams")
                           (map ui-team teams))))))

    ,))

(comment ; VARIANT 7.5 - load marker
  (do
    ;; BEWARE: Only the changed parts presented here, the rest is the same as above.
    ;; <THE SAME CODE AS ABOVE EXCLUDING Root7 HERE...>
    (defsc Root7 [this {teams :all-teams :as props}]
      {:query [{:all-teams (comp/get-query Team)} [df/marker-table :teams]]}
      (div
        (button {:type "button"
                 :onClick #(df/load! this :teams Team {:target (targeting/replace-at [:all-teams])
                                                       :marker :teams})}
                "Load data")
        (let [marker (get props [df/marker-table :teams])]
          (cond
            (df/loading? marker) (p "Loading...")
            ;; ...
            :else
            (comp/fragment (h1 "Teams")
                           (map ui-team teams))))))
    ,))

(comment ; 8 Fix the graph
  (do
    (defsc Menu [this {:keys [cities selected-city]}]
      {:ident (fn [] [:component/id ::Menu])
       :query [:cities :selected-city]
       :initial-state {}}
      ;; Note: This is not a very good way of using a select :-) 
      (dom/select {:value (or selected-city "Select one:")
                   :onChange #(do (println "Selected city:" (.-value (.-target %)))
                                (m/set-string! this :selected-city :event %))}
        (->> (cons "Select one:" cities)
             (mapv #(dom/option {:key %, :value %} %)))))

    (def ui-menu (comp/factory Menu))

    (defsc Root8 [_ props]
      ;; The key discovery is that we can add _arbitrary_ "edges" to the client DB / graph
      ;; (here Root -(:menu)-> Menu) f.ex. by setting them up via :initial-state
      {:query [{:menu (comp/get-query Menu)}]
       :initial-state {:menu {}}}
      (dom/div
        (h1 "Select a city!")
        (ui-menu (:menu props))))

    (defresolver cities [_ _]
      {::pc/input #{}
       ::pc/output [:cities]}
      {:cities ["Linköping" "Oslo" "Prague"]})

    (def app8 (config-and-render! Root8 {:resolvers [cities]}))

    (df/load! app8 :cities nil {:target (conj (comp/get-ident Menu {}) :cities)})
    ))