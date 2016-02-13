(ns cljs-sandbox.core
  (:require [reagent.core :as r]
            [schema.core :as s])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [schema.core :as sm])
  (:use [cljs.core.async :only [chan <! >! put!]]))

(sm/defschema Square {:x           s/Int
                      :y           s/Int
                      :side-length s/Int
                      ; oh god i don't know how to do math what am i signing up for
                      :angle       s/Num
                      :speed       s/Num})

(sm/defn generate-square []
  ; TODO use document width
  {:x           (rand-int 1000)
   :y           (rand-int 1000)
   :side-length (max 10 (rand-int 100))
   :angle       (rand-int 360)
   :speed       (rand-int 10)})

(sm/defn next-square-state :- Square
  [sq :- Square]
  (-> sq
      (update-in :x (* (:speed sq) (Math/cos (:angle sq))))
      (update-in :y (* (:speed sq) (Math/sin (:angle sq)))))
  {:x (+ (:x sq))}
  )

(defn draw-square [square]
  [:rect {:x (square :x)
          :y (square :y)
          :width (square :side-length)
          :height (square :side-length)}]
  )

(defn draw-state [state]
  [:svg {:width 1000 :height 1000}
   (let [state @state]
     (for [[index square] (map-indexed vector (:squares state))]
       ^{:Key (str "square-" index)} [draw-square square]))])

(def state {:squares (repeatedly 10 generate-square)})

(defn ^:export main []
  (r/render-component [draw-state state]
                      (js/document.getElementById "content"))


  ; TODO - kill all goblocks, start up new ones
  )


(comment
  )
