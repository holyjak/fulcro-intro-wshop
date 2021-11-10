(ns fulcro-todomvc.ui
  (:require
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.algorithms.tempid :as tmp]
    [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :as mut :refer [defmutation]]
    [fulcro-todomvc.api :as api]
    [fulcro-todomvc.app :refer [app]]
    [goog.object :as gobj]
    [cljs.pprint :as pprint]))

(defn is-enter? [evt] (= 13 (.-keyCode evt)))
(defn is-escape? [evt] (= 27 (.-keyCode evt)))

(defn trim-text
  "Returns text without surrounding whitespace if not empty, otherwise nil"
  [text]
  (let [trimmed-text (clojure.string/trim text)]
    (when-not (empty? trimmed-text)
      trimmed-text)))

(defsc TodoItem [this
                 {:ui/keys   [ui/editing ui/edit-text]
                  :item/keys [id label complete] :or {complete false} :as props}
                 {:keys [delete-item]}]
  {:query              [:item/id :item/label :item/complete :ui/editing :ui/edit-text]
   :ident              :item/id
   :initLocalState     (fn [this] {:save-ref (fn [r] (gobj/set this "input-ref" r))})
   :componentDidUpdate (fn [this prev-props _]
                         (when (and (not (:ui/editing prev-props))
                                 (:ui/editing (comp/props this)))
                           (let [input-field        (gobj/get this "input-ref")
                                 input-field-length (when input-field (.. input-field -value -length))]
                             (when input-field
                               (.focus input-field)
                               (.setSelectionRange input-field 0 input-field-length)))))}
  (let [submit-edit (fn [evt]
                      (if-let [trimmed-text (trim-text (.. evt -target -value))]
                        (do
                          (comp/transact! this [(api/commit-label-change {:id id :text trimmed-text})])
                          (mut/set-string! this :ui/edit-text :value trimmed-text)
                          (mut/toggle! this :ui/editing))
                        (delete-item id)))]

    (dom/li {:classes [(when complete (str "completed")) (when editing (str " editing"))]}
      (dom/div :.view {}
        (dom/input {:type      "checkbox"
                    :className "toggle"
                    :checked   (boolean complete)
                    :onChange  (fn []
                                 ;; The only-refresh is used to make sure the list re-renders, as
                                 ;; it does a calculated rendering of the "all checked" checkbox.
                                 (let [tx (if complete [(api/todo-uncheck {:id id})] [(api/todo-check {:id id})])]
                                   (comp/transact! this tx {:only-refresh [(comp/get-ident this)]})))})
        (dom/label {:onDoubleClick (fn []
                                     (mut/toggle! this :ui/editing)
                                     (mut/set-string! this :ui/edit-text :value label))} label)
        (dom/button :.destroy {:onClick #(delete-item id)}))
      (dom/input {:ref       (comp/get-state this :save-ref)
                  :className "edit"
                  :value     (or edit-text "")
                  :onChange  #(mut/set-string! this :ui/edit-text :event %)
                  :onKeyDown #(cond
                                (is-enter? %) (submit-edit %)
                                (is-escape? %) (do (mut/set-string! this :ui/edit-text :value label)
                                                   (mut/toggle! this :ui/editing)))
                  :onBlur    #(when editing (submit-edit %))}))))

(def ui-todo-item (comp/computed-factory TodoItem {:keyfn :item/id}))

(defn header [component title]
  (let [{:keys [list/id ui/new-item-text]} (comp/props component)]
    (dom/header :.header {}
      (dom/h1 {} title)
      (dom/input {:value       (or new-item-text "")
                  :className   "new-todo"
                  :onKeyDown   (fn [evt]
                                 (when (is-enter? evt)
                                   (when-let [trimmed-text (trim-text (.. evt -target -value))]
                                     (comp/transact! component `[(api/todo-new-item ~{:list-id id
                                                                                      :id      (tmp/tempid)
                                                                                      :text    trimmed-text})]))))
                  :onChange    (fn [evt] (mut/set-string! component :ui/new-item-text :event evt))
                  :placeholder "What needs to be done?"
                  :autoFocus   true}))))

(defn filter-footer [component num-todos num-completed]
  (let [{:keys [list/id list/filter]} (comp/props component)
        num-remaining (- num-todos num-completed)]
    (dom/footer :.footer {}
      (dom/span :.todo-count {}
        (dom/strong (str num-remaining " left")))
      (dom/ul :.filters {}
        (dom/li {}
          (dom/a {:className (when (or (nil? filter) (= :list.filter/none filter)) "selected")
                  :href      "#"
                  :onClick   #(comp/transact! component `[(api/todo-filter {:filter :list.filter/none})])} "All"))
        (dom/li {}
          (dom/a {:className (when (= :list.filter/active filter) "selected")
                  :href      "#/active"
                  :onClick   #(comp/transact! component `[(api/todo-filter {:filter :list.filter/active})])} "Active"))
        (dom/li {}
          (dom/a {:className (when (= :list.filter/completed filter) "selected")
                  :href      "#/completed"
                  :onClick   #(comp/transact! component `[(api/todo-filter {:filter :list.filter/completed})])} "Completed")))
      (when (pos? num-completed)
        (dom/button {:className "clear-completed"
                     :onClick   #(comp/transact! component `[(api/todo-clear-complete {:list-id ~id})])} "Clear Completed")))))


(defn footer-info []
  (dom/footer :.info {}
    (dom/p {} "Double-click to edit a todo")
    (dom/p {} "Created by "
      (dom/a {:href   "http://www.fulcrologic.com"
              :target "_blank"} "Fulcrologic, LLC"))
    (dom/p {} "Part of "
      (dom/a {:href   "http://todomvc.com"
              :target "_blank"} "TodoMVC"))))

(defsc TodoList [this {:list/keys [id items filter title] :as props}]
  {:ident         :list/id
   :query         [:list/id :ui/new-item-text {:list/items (comp/get-query TodoItem)} :list/title :list/filter]}
  (let [num-todos       (count items)
        completed-todos (filterv :item/complete items)
        num-completed   (count completed-todos)
        all-completed?  (every? :item/complete items)
        filtered-todos  (case filter
                          :list.filter/active (filterv (comp not :item/complete) items)
                          :list.filter/completed completed-todos
                          items)
        delete-item     (fn [item-id] (comp/transact! this `[(api/todo-delete-item ~{:list-id id :id item-id})]))]
    (dom/div {}
      (dom/section :.todoapp {}
        (header this title)
        (when (pos? num-todos)
          (dom/div {}
            (dom/section :.main {}
              (dom/input {:type      "checkbox"
                          :className "toggle-all"
                          :checked   all-completed?
                          :onClick   (fn [] (if all-completed?
                                              (comp/transact! this `[(api/todo-uncheck-all {:list-id ~id})])
                                              (comp/transact! this `[(api/todo-check-all {:list-id ~id})])))})
              (dom/label {:htmlFor "toggle-all"} "Mark all as complete")
              (dom/ul :.todo-list {}
                (map #(ui-todo-item % {:delete-item delete-item}) filtered-todos)))
            (filter-footer this num-todos num-completed))))
      (footer-info))))

(def ui-todo-list (comp/factory TodoList))

(defsc User [_ _]
  {:initial-state {}
   :ident (fn [] [:component/id :User])
   :query [[:logged-in-user '_]]}
  nil)

(defsc Root [this {:root/keys [current-list user] :as props}]
  {:initial-state {:root/user {}}
   :query         [{:root/current-list (comp/get-query TodoList)} {:root/user (comp/get-query User)}]}
  (dom/div {}
    ((comp/factory User) user)
    (ui-todo-list current-list)))

(comment
  (tap> (comp/props (comp/class->any app Root)))
  (com.fulcrologic.fulcro.algorithms.merge/merge!
    app {:logged-in-user #:user{:name "jo" :fname "Jo"}} ['*])
  ;; Exercise 3.1
  (comp/get-query Root) ; TodoList, Root
  ;; Exercise 3.2
  (-> (comp/ident->components app [:item/id 2])
      first
      (comp/props)
      ;; BEWARE: Open Shadow-cljs Inspect to see the output, see the instructions
      (tap>))
  ;; _Exercise 3.5
  (binding [*print-meta* true] (tap> (pr-str (comp/get-query TodoList))))

  ;; Exercise 4.1
  (comp/get-query Root)
  ;; Exercise 4.2
  (tap> (comp/get-query Root))
  ;; Exercise 4.3
  (let [state (app/current-state app)] ; state = Client DB current value
    (tap> (fdn/db->tree
            (comp/get-query Root)
            state state)))

  ;; Exercise 5.4
  (tap> (app/current-state app))

  ;; Exercise 7.1
  (df/load! app [:list/id 2] TodoList)

  ;; Exercise 9.1
  (comp/transact! app [(api/todo-uncheck {:id 3})])
  ;; Exercise 9.2
  (comp/transact! app [(api/todo-check {:id 3})]))
  


  
