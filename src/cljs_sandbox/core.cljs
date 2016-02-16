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

; TODO use document width/height
(def WIDTH 1000)
(def HEIGHT 500)

(sm/defn generate-square []
  {:x           (rand-int (- WIDTH 25))
   :y           (rand-int (- HEIGHT 25))
   :side-length (max 10 (rand-int 25))
   :fill        (random-hex-color-string)
   :angle       (* (Math/random) 2 Math/PI)
   :speed       1 #_(max 1 (rand-int 10))})

(sm/defn x-axis-in-bounds? :- s/Bool
  [sq :- Square]
  (< 0 (sq :x) (+ (sq :side-length) (sq :x)) WIDTH))

(sm/defn y-axis-in-bounds? :- s/Bool
  [sq :- Square]
  (< 0 (sq :y) (+ (sq :side-length) (sq :y)) HEIGHT))

(sm/defn in-bounds? :- s/Bool
  [sq :- Square]
  (and (x-axis-in-bounds? sq) (y-axis-in-bounds? sq)))

(sm/defn next-square-state :- Square
  [sq :- Square]
  (js/console.log "next-square-state")
  (let [next-axis-value (sm/fn [sq :- Square
                                axis :- (s/enum :x :y)]
                          (let [math-fn ({:x Math/cos :y Math/sin} axis)]
                            (+ (sq axis)
                               (* (sq :speed) (math-fn (sq :angle))))))
        next-sq (assoc sq
                  :x (next-axis-value sq :x)
                  :y (next-axis-value sq :y))]
    (if (in-bounds? next-sq)
      next-sq
      (let [big-angle (cond
                        (and (not (x-axis-in-bounds? next-sq))
                             (not (y-axis-in-bounds? next-sq))) (* Math/PI 6)
                        (not (y-axis-in-bounds? next-sq)) (* Math/PI 4)
                        (not (x-axis-in-bounds? next-sq)) (* Math/PI 2))]
        (next-square-state (assoc sq :angle (- big-angle (sq :angle))))))))

(defn draw-square [square]
  (js/console.log "draw-square")
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

  ; TODO - kill all previous goblocks on reload

  (let [ch (chan)]
    (loop [i 0]
      (when (< i 5 #_(count (:squares @state)))
        (go (while true
              ; TODO - have these guys add to a channel, and have a single goblock that dequeues and swap!s
              (<! (timeout (+ 500 (rand-int 50))))
              (>! ch i)
              (swap! state update-in [:squares i] next-square-state)))
        (recur (inc i))))

    (go (while true
          (let [idx (<! ch)]
            (swap! state update-in [:squares idx] next-square-state))))

    ))

