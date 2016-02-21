(ns cljs-sandbox.boi
  (:require [reagent.core :as r]
            [schema.core :as s]
            [goog.events :as events])
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
  )

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
  (events/listen
    (.-body js/document)
    (.-KEYDOWN events/EventType)
    (fn [event]
      (.preventDefault event)
      (put! event-chan event)
      false))

  (go-loop []
    (let [msg (<! event-chan)]
      (js/console.log (.-keyCode msg))
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

    (handle-events state event-chan)

    )
  )


; TODO - tear down old event listeners / old goloop

(comment


  )
