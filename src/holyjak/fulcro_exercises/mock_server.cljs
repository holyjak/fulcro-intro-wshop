(ns holyjak.fulcro-exercises.mock-server
  "Simulate a Pathom backend but running in the browser

  Source: https://github.com/fulcrologic/fulcro-developer-guide/blob/master/src/book/book/pathom.cljs"
  (:require
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.networking.mock-server-remote :refer [mock-http-server]]
    [com.wsscode.pathom.core :as p]
    [com.wsscode.pathom.connect :as pc]))

(pc/defresolver index-explorer [env _]
  {::pc/input  #{:com.wsscode.pathom.viz.index-explorer/id}
   ::pc/output [:com.wsscode.pathom.viz.index-explorer/index]}
  {:com.wsscode.pathom.viz.index-explorer/index
   (-> (get env ::pc/indexes)
       (update ::pc/index-resolvers #(into {} (map (fn [[k v]] [k (dissoc v ::pc/resolve)])) %))
       (update ::pc/index-mutations #(into {} (map (fn [[k v]] [k (dissoc v ::pc/mutate)])) %)))})

(defn new-parser [my-resolvers]
  (p/parallel-parser
    {::p/env     {::p/reader [p/map-reader
                              pc/parallel-reader
                              pc/open-ident-reader]}
     ::p/mutate  pc/mutate-async
     ::p/plugins [(pc/connect-plugin {::pc/register (conj my-resolvers index-explorer)})
                  p/error-handler-plugin
                  p/request-cache-plugin
                  (p/post-process-parser-plugin p/elide-not-found)]}))

(defn mock-remote
  ([resolvers env]
   (let [parser    (new-parser resolvers)
         transmit! (:transmit! (mock-http-server {:parser (fn [req] (parser env req))}))]
     {:remote {:transmit! (fn [this send-node]
                            (js/setTimeout
                              #(transmit! this send-node)
                              500))}}))
  ([resolvers]
   (mock-remote resolvers {})))
