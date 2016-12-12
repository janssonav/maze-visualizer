(ns maze-visualization.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))

(def testmaze [[1  :S 1  1  1  1  1]
               [1  0  0  0  0  0  1]
               [1  1  1  1  1  0  1]
               [1  0  0  0  0  0  1]
               [1  0  1  1  1  0  1]
               [1  0  0  0  1  0  1]
               [1  1  1  :E 1  1  1]])

(defn cross [color i j]
  [:g {:stroke color 
       :stroke-width 0.4
       :stroke-linecap "round"
       :transform
       (str "translate(" (+ 0.5 i) "," (+ 0.5 j) ") "
            "scale(0.3)")}
   [:line {:x1 -1 :y1 -1 :x2 1 :y2 1}]
   [:line {:x1 1 :y1 -1 :x2 -1 :y2 1}]])

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

(def start-component   (partial cross  "darkred"))
(def end-component     (partial cross  "green"))
(def wall-component    (partial rect   "blue"))
(def floor-component   (partial rect   "white"))
(def visited-component (partial circle "blue"))

;; Draws the maze
(defn maze-component [maze]
  (let [tiles-y (count maze)
        tiles-x  (count (first maze))]
    (into
      [:svg
       {:view-box (str "0 0 " tiles-x " " tiles-y)
        :width 500
        :height 500}]
      (for [i (range tiles-x)
            j (range tiles-y)]
            (case (get-in maze [j i])
              :S [start-component   i j]
              :E [end-component     i j]
              1  [wall-component    i j]
              0  [floor-component   i j]
              :x [visited-component i j])))))

;; -------------------------
;; Views


(defn home-page-1 []
  [:div [:h2 "Kikkelis kokkeli"]
   [:div [:a {:href "/about"} "go to about page"]]])

;;(defn about-page []
;;  [:div [:h2 "About maze-visualization"]
;;   [:div [:a {:href "/"} "go to the home page"]]])

(defn home-page-2 []
  [:div
   [:h2 "Sokkelo"]
   (maze-component testmaze)])

(defn current-page []
  [:div [(session/get :current-page)]])

(def home-page home-page-2)

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

;;(secretary/defroute "/about" []
;;  (session/put! :current-page #'about-page))

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
