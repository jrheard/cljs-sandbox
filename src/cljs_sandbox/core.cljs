(ns cljs-sandbox.core
  (:require [reagent.core :as r]
            [schema.core :as s])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [schema.core :as sm])
  (:use [cljs.core.async :only [chan <! >! put! timeout]]))

(sm/defschema Square {:x           s/Int
                      :y           s/Int
                      :side-length s/Int
                      ; oh god i don't know how to do math what am i signing up for
                      :angle       s/Num
                      :speed       s/Num})

(defn random-hex-color-string []
  ; via https://gist.github.com/martinklepsch/8730542
  (str "#" (.toString (rand-int 16rFFFFFF) 16)))

(sm/defn generate-square []
  ; TODO use document width
  {:x           (rand-int 1000)
   :y           (rand-int 500)
   :side-length (max 10 (rand-int 25))
   :fill        (random-hex-color-string)
   :angle       (rand-int 360)
   :speed       (max 1 (rand-int 10))})

(sm/defn next-square-state :- Square
  [sq :- Square]
  (js/console.log "sq is " (clj->js sq))
  (-> sq
      (update-in [:x] #(+ % (* (:speed sq) (Math/cos (:angle sq)))))
      (update-in [:y] #(+ % (* (:speed sq) (Math/sin (:angle sq))))))
  )

(defn draw-square [square]
  (js/console.log (clj->js square))
  [:rect {:x      (square :x)
          :y      (square :y)
          :fill   (square :fill)
          :width  (square :side-length)
          :height (square :side-length)}])

(defn draw-state [state]
  [:svg {:width 1000 :height 500}
   (let [state @state]
     (for [[index square] (map-indexed vector (:squares state))]
       ^{:key (str "square-" index)} [draw-square square]))])

(def state (r/atom {:squares (apply vector (repeatedly 100 generate-square))}))

(defn ^:export main []
  (r/render-component [draw-state state]
                      (js/document.getElementById "content"))

  (loop [i 0]
    (when (< i (count (:squares @state)))
      (go (while true
            (<! (timeout (+ 50 (rand-int 50))))
            (swap! state update-in [:squares i] next-square-state)))
      (recur (inc i)))))

