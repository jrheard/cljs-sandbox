(ns cljs-sandbox.boi
  (:require [reagent.core :as r]
            [schema.core :as s]
            [goog.events :as events])
  (:import [goog.events KeyCodes])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [schema.core :as sm])
  (:use [cljs.core.async :only [chan <! >! put! timeout]]))

(sm/defschema Player {:x s/Int
                      :y s/Int})

(sm/defn make-player :- Player
  []
  {:x 10
   :y 10})

(sm/defn move :- Player
  [player :- Player
   direction :- (s/enum :left :right :up :down)]
  (js/console.log move)
  (let [[axis amount] (case direction
                        :left [:x -1]
                        :right [:x 1]
                        :up [:y -1]
                        :down [:y 1])]
    (update-in player [axis] + amount)))

(sm/defn draw-player [player :- Player]
  [:rect {:x      (player :x)
          :y      (player :y)
          :width  25
          :height 25}])

(defn draw-state [state]
  (let [state @state]
    [:svg {:width 1000 :height 1000}
     [draw-player (state :player)]]))

(defonce state (r/atom {:player (make-player)}))

(def move-keys #{KeyCodes.DOWN KeyCodes.LEFT KeyCodes.UP KeyCodes.RIGHT})

(defn listen-to-keyboard-inputs [event-chan]
  (events/removeAll (.-body js/document))
  (events/listen
    (.-body js/document)
    (.-KEYDOWN events/EventType)
    (fn [event]
      (let [code (.-keyCode event)]
        (when (contains? move-keys code)
          (.preventDefault event)
          (put! event-chan {:type      :move
                            :direction ({KeyCodes.DOWN  :down
                                         KeyCodes.UP    :up
                                         KeyCodes.LEFT  :left
                                         KeyCodes.RIGHT :right} code)}))))))

(defn handle-events [state event-chan]
  (listen-to-keyboard-inputs event-chan)

  (go-loop []
    (let [msg (<! event-chan)]
      (swap! state update-in [:player] move (:direction msg))
      (js/console.log (clj->js (:direction msg)))
      (recur))))

(defn ^:export main []
  (let [event-chan (chan)]
    (r/render-component [draw-state state]
                        (js/document.getElementById "content"))

    (handle-events state event-chan)))


; TODO - rather than listening to each keyboard event and eg moving right every time we see a :right event,
; keep track of the player's direction in state
; when the key is raised again, fire a :stop-moving
; and have a go-loop that swap!s (move player direction magnitude) using (r/next-tick)
; per https://reagent-project.github.io/news/binary-clock.html
