(ns cljs-sandbox.boi
  (:require [reagent.core :as r]
            [schema.core :as s]
            [goog.events :as events]
            [goog.events.KeyCodes :as key-codes])
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

(defn handle-events [state event-chan]
  (events/removeAll (.-body js/document))

  (events/listen
    (.-body js/document)
    (.-KEYDOWN events/EventType)
    (fn [event]
      (.preventDefault event)
      (js/console.log event)
      (put! event-chan (.-keyCode event))
      false))

  (go-loop []
    (let [msg (<! event-chan)]
      (js/console.log msg)
      (recur)
      #_(case (:type msg)
          :move (swap! state move (msg :direction))

          (recur)))
    )
  )

(defn ^:export main []
  (let [event-chan (chan)]
    (r/render-component [draw-state state]
                        (js/document.getElementById "content"))

    (js/console.log key-codes)
    (handle-events state event-chan)))

(comment
  (.-DOWN events/Key)
  events/Key.DOWN

  events/KeyCodes

  (.-DOWN key-codes)
  key-codes/SPACE

  goog.events.KeyCodes/DOWN

  goog.events.KeyCodes
  )
