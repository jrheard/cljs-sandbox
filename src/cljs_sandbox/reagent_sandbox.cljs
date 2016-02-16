(ns cljs-sandbox.reagent-sandbox
  (:require [reagent.core :as r]))

(def state (r/atom [1 2 3]))

(defn draw-item [item]
  (js/console.log "draw-item")
  [:p item])

(defn draw-paragraphs [state]
  (let [state @state]
    [:div
     (for [[index item] (map-indexed vector state)]
       ^{:key (str "thing-" index)} [draw-item item])]))

(defn draw-rect [index item]
  (js/console.log "draw-rect")
  [:svg
   [:rect {:width 20 :height 20 :x 50 :y (* index 50) :stroke "red"}]
   [:text {:x 55 :y (+ 17 (* index 50)) :fill "white"} item]])

(defn draw-rects [state]
  (js/console.log "draw-rects")
  (let [state @state]
    [:svg {:width 500 :height 500}
     [:rect {:x 0 :y 0 :width 500 :height 500 :fill "white" :stroke "black"}]
     (for [[index item] (map-indexed vector state)]
       ^{:key (str "thing-" index)} [draw-rect index item])]))

(defn ^:export main []
  (r/render-component [draw-rects state]
                      (js/document.getElementById "content")))

; .... well, so far when you update this atom, only the relevant rectangle gets redrawn, not all of 'em.
(comment
  (swap! state assoc 1 4)
  )
