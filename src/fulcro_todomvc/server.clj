(ns fulcro-todomvc.server
  (:require
    [clojure.core.async :as async]
    [com.fulcrologic.fulcro.algorithms.do-not-use :as util]
    [com.fulcrologic.fulcro.server.api-middleware :as fmw :refer [not-found-handler wrap-api]]
    [com.wsscode.pathom.connect :as pc]
    [com.wsscode.pathom.core :as p]
    [immutant.web :as web]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.not-modified :refer [wrap-not-modified]]
    [ring.middleware.resource :refer [wrap-resource]]
    [ring.util.response :refer [content-type response file-response resource-response]]
    [taoensso.timbre :as log]
    [clojure.tools.namespace.repl :as tools-ns]
    [com.fulcrologic.fulcro.algorithms.tempid :as tempid]))

(def item-db (atom {1 {:item/id       1
                       :item/label    "Item 1"
                       :item/complete false}
                    2 {:item/id       2
                       :item/label    "Item 2"
                       :item/complete false}
                    3 {:item/id       3
                       :item/label    "Item 3"
                       :item/complete false}}))

(pc/defmutation todo-new-item [env {:keys [id list-id text]}]
  {::pc/sym    `fulcro-todomvc.api/todo-new-item
   ::pc/params [:list-id :id :text]
   ::pc/output [:item/id]}
  (log/info "New item on server")
  (let [new-id (tempid/uuid)]
    (swap! item-db assoc new-id {:item/id new-id :item/label text :item/complete false})
    {:tempids {id new-id}
     :item/id new-id}))

(pc/defmutation todo-check [env {:keys [id]}]
  {::pc/sym    `fulcro-todomvc.api/todo-check
   ::pc/params [:id]
   ::pc/output []}
  (log/info "Checked item" id)
  (swap! item-db assoc-in [id :item/complete] true)
  {})

(pc/defmutation todo-uncheck [env {:keys [id]}]
  {::pc/sym    `fulcro-todomvc.api/todo-uncheck
   ::pc/params [:id]
   ::pc/output []}
  (log/info "Unchecked item" id)
  (swap! item-db assoc-in [id :item/complete] false)
  {})

(pc/defmutation commit-label-change [env {:keys [id text]}]
  {::pc/sym    `fulcro-todomvc.api/commit-label-change
   ::pc/params [:id :text]
   ::pc/output []}
  (log/info "Set item label text of" id "to" text)
  (swap! item-db assoc-in [id :item/label] text)
  {})

(pc/defmutation todo-delete-item [env {:keys [id]}]
  {::pc/sym    `fulcro-todomvc.api/todo-delete-item
   ::pc/params [:id]
   ::pc/output []}
  (log/info "Deleted item" id)
  (swap! item-db dissoc id)
  {})

(defn- to-all-todos [db f]
  (into {}
    (map (fn [[id todo]]
           [id (f todo)]))
    db))

(pc/defmutation todo-check-all [env _]
  {::pc/sym    `fulcro-todomvc.api/todo-check-all
   ::pc/params []
   ::pc/output []}
  (log/info "Checked all items")
  (swap! item-db to-all-todos #(assoc % :item/complete true))
  {})

(pc/defmutation todo-uncheck-all [env _]
  {::pc/sym    `fulcro-todomvc.api/todo-uncheck-all
   ::pc/params []
   ::pc/output []}
  (log/info "Unchecked all items")
  (swap! item-db to-all-todos #(assoc % :item/complete false))
  {})

(pc/defmutation todo-clear-complete [env _]
  {::pc/sym    `fulcro-todomvc.api/todo-clear-complete
   ::pc/params []
   ::pc/output []}
  (log/info "Cleared completed items")
  (swap! item-db (fn [db] (into {} (remove #(-> % val :item/complete)) db)))
  {})

;; Support for Fulcro Inspect - Index Explorer
(pc/defresolver index-explorer
  "This resolver is necessary to make it possible to use 'Load index' in Fulcro Inspect - EQL"
  [env _]
  {::pc/input  #{:com.wsscode.pathom.viz.index-explorer/id}
   ::pc/output [:com.wsscode.pathom.viz.index-explorer/index]}
  {:com.wsscode.pathom.viz.index-explorer/index
   (-> (get env ::pc/indexes)
       (update ::pc/index-resolvers #(into {} (map (fn [[k v]] [k (dissoc v ::pc/resolve)])) %))
       (update ::pc/index-mutations #(into {} (map (fn [[k v]] [k (dissoc v ::pc/mutate)])) %)))})

;; How to go from :list/id to that list's details
(pc/defresolver list-resolver [env {id :list/id :as params}]
  {::pc/input  #{:list/id}
   ::pc/output [:list/title {:list/items [:item/id]}]}
  ;; normally you'd pull the list from the db, and satisfy the listed
  ;; outputs. For demo, we just always return the same list details.
  (case id
    1 {:list/title "The List"
       :list/items (into [] (sort-by :item/id (vals @item-db)))}
    2 {:list/title "Another List"
       :list/items [{:item/id 99, :item/label "Hardcoded item", :item/complete true}
                    (assoc (get @item-db 1)
                      :item/label "Item 1 - re-loaded from server")]}))

;; how to go from :item/id to item details.
(pc/defresolver item-resolver [env {:keys [item/id] :as params}]
  {::pc/input  #{:item/id}
   ::pc/output [:item/complete :item/label]}
  (get @item-db id))

;; define a list with our resolvers
(def my-resolvers [index-explorer
                   ;; app resolvers:
                   list-resolver item-resolver
                   ;; mutations:
                   todo-new-item commit-label-change todo-delete-item
                   todo-check todo-uncheck
                   todo-check-all todo-uncheck-all
                   todo-clear-complete])

;; setup for a given connect system
(def parser
  (p/parser
    {::p/env     {::p/reader                 [p/map-reader
                                              pc/reader2
                                              pc/open-ident-reader]
                  ::pc/mutation-join-globals [:tempids]}
     ::p/mutate  pc/mutate
     ::p/plugins [(pc/connect-plugin {::pc/register my-resolvers})
                  (p/post-process-parser-plugin p/elide-not-found)
                  p/error-handler-plugin]}))

(defn wrap-index [handler]
  (fn [req]
    (if (= "/" (:uri req))
      (-> (handler (assoc req :uri "/index.html", :path-info "/index.html"))
          (content-type "text/html"))
      (handler req))))

(def middleware (-> not-found-handler
                  (wrap-api {:uri    "/api"
                             :parser (fn [query] (parser {} query))})
                  (fmw/wrap-transit-params)
                  (fmw/wrap-transit-response)
                  (wrap-resource "public")
                  wrap-index
                  wrap-content-type
                  wrap-not-modified))

(defonce server (atom nil))

(defn http-server []
  (let [result (web/run middleware {:host "0.0.0.0"
                                    :port 8181})]
    (reset! server result)
    ((requiring-resolve 'clojure.java.browse/browse-url) "http://localhost:8181")
    (fn [] (web/stop result))))

(comment

  ;; Calva setup: Execute the line below to start the server
  ;; (Alt-Enter or, on Mac, Option-Enter; if it does not work, make sure to
  ;; put your cursor after the closing parenthese)
  (http-server)
  ;; You should see output like:
  ;; => #function[fulcro-todomvc.server/http-server/fn--63504]
  ;; if you see instead `#function[fulcro-todomvc.server/http-server]` then
  ;; you have not run the function, only evaluated the var


  (web/stop @server)


  (tools-ns/refresh-all)
  (async/<!! (parser {} `[(fulcro-todomvc.api/todo-new-item {:id 2 :text "Hello"})]))

  @item-db

  )
