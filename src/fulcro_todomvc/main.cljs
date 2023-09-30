(ns fulcro-todomvc.main
  (:require
    [fulcro-todomvc.ui :as ui]
    [com.fulcrologic.fulcro.networking.websockets :as fws]
    [com.fulcrologic.fulcro.algorithms.timbre-support :refer [console-appender prefix-output-fn]]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [taoensso.timbre :as log]
    [fulcro-todomvc.app :refer [app]]))

(defn ^:export start []
  (app/mount! app ui/Root "app")
  (df/load! app [:list/id 1] ui/TodoList
    {:target [:root/current-list]})
  (log/merge-config! {:output-fn prefix-output-fn
                      :appenders {:console (console-appender)}}))

(comment
  (app/set-root! app ui/Root {:initialize-state? true})
  (app/mounted? app)
  (df/load! app [:list/id 1] ui/TodoList)
  (app/mount! app ui/Root "app" {:initialize-state? false})
  @(::app/state-atom app)
  ;(fws/stop! remote)
  )
