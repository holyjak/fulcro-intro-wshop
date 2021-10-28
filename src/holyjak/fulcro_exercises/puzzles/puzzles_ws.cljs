(ns holyjak.fulcro-exercises.puzzles.puzzles-ws
  "After you have completed the exercises, you can continue
  with these puzzles - pieces of code that you are expected to fix.

  See the README.md for more information."
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [nubank.workspaces.core :as ws]
    [nubank.workspaces.model :as wsm]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro.dom :as dom]))

;;----------------------------------------------------------------------------------
;; PUZZLE 1
(m/defmutation toggle-color [_]
  (action [{:keys [state]}]
          (swap! state update :ui/red? not)))

(defsc ColorChangingSquare [this {:ui/keys [red?]}]
   {:query [:ui/red?]
    :ident (fn [] [:component/id :ColorChangingSquare])
    :initial-state {:ui/red? false}}
   (dom/div {:style {:backgroundColor (if red? "red" "blue")
                     :padding "1em"
                     :color "white"}}
      (dom/p "The button bellow should change the background color from blue
             to red (and back) but it does not work. Fix it.")
      (dom/button {:onClick #(comp/transact! this [(toggle-color)])
                   :style {:backgroundColor "unset"
                           :color "white"}}
                (str "Make " (if red? "blue" "red")))))

(ws/defcard p1-change-background-button-puzzle
            {::wsm/card-width 2 ::wsm/card-height 6}
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root       ColorChangingSquare
               ::ct.fulcro/wrap-root? true}))

;;----------------------------------------------------------------------------------
;; PUZZLE ?