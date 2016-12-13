(ns maze-visualization.core
    (:require [maze-visualization.solver :as solver]
              [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))

(def testmaze [[1  :S 1  1  1  1  1  1  1]
               [1  0  0  0  0  0  0  0  1]
               [1  1  1  1  0  1  1  0  1]
               [1  0  0  0  0  0  1  0  1]
               [1  0  1  1  1  0  1  0  1]
               [1  0  1  0  0  0  1  0  1]
               [1  0  1  1  1  1  1  0  1]
               [1  0  0  0  1  0  0  0  1]
               [1  0  1  0  1  0  1  1  1]
               [1  0  1  0  0  0  0  0  1]
               [1  1  1  :E 1  1  1  1  1]])

(def initial-state 
  {:maze testmaze
   :solved false})

(defonce app-state
  (atom initial-state))

(defn solve [state]
  (let [solved (solver/solve-maze (:maze state))]
    (if (nil? solved)
      state
      {:maze solved
       :solved true})))

(defn set-solved-state! []
  (swap! app-state solve))

(defn reset-state! []
  (reset! app-state initial-state))

(defn cross [color i j]
  [:g {:stroke color 
       :stroke-width 0.4
       :stroke-linecap "round"
       :transform
       (str "translate(" (+ 0.5 i) "," (+ 0.5 j) ") "
            "scale(0.3)")}
   [:line {:x1 -1 :y1 -1 :x2  1 :y2 1}]
   [:line {:x1  1 :y1 -1 :x2 -1 :y2 1}]])

(defn rect [color i j]
  [:rect {:fill color
          :x i
          :y j
          :width 1
          :height 1}])

(defn circle [color i j]
  [:circle {:fill color
            :cx (+ 0.5 i)
            :cy (+ 0.5 j)
            :r 0.4}])

(def draw-fn {1  (partial rect   "blue")
              0  (partial rect   "white")
              :x (partial circle "green")
              :S (partial cross  "darkred")
              :E (partial cross  "green")})

(defn maze-component 
  "Draws the given maze"
  [maze]
  (let [tiles-y (count maze)
        tiles-x (count (first maze))]
    (into
      [:svg
       {:view-box (str "0 0 " tiles-x " " tiles-y)
        :width 700
        :height 700}]
      (for [i (range tiles-x)
            j (range tiles-y)]
        [(draw-fn (get-in maze [j i])) i j]))))

;; -------------------------
;; Views

(defn action-button []
  (if (:solved @app-state)
    [:button {:on-click reset-state!} "RESET"]
    [:button {:on-click set-solved-state!} "SOLVE"]))

(defn home-page []
  [:div
   [:div (action-button)]
   (maze-component (:maze @app-state))])


(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
