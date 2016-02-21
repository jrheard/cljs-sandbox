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


;;;; from https://github.com/Day8/re-frame/wiki/When-do-components-update%3F

(defn greet-number
  "I say hello to an integer"
  [num]                             ;; an integer
  (js/console.log "greet-number")
  [:div (str "Hello #" num)])       ;; [:div "Hello #1"]


(defn more-button
  "I'm a button labelled 'More' which increments counter when clicked"
  [counter]                             ;; a ratom
  (js/console.log "more-button")
  [:div  {:class "button-class"
          :on-click  #(swap! counter inc)}   ;; increment the int value in counter
   "More"])


(defn parent
  []
  (js/console.log "parent init")
  (let [counter  (reagent.ratom/atom 1)]    ;; the render closes over this state
    (fn  parent-renderer
      []
      (js/console.log "parent run")
      [:div
       [more-button counter]            ;; no @ on counter
       [greet-number @counter]])))      ;; notice the @. The prop is an int

(defn ^:export main []
  (r/render-component [parent]

                      (js/document.getElementById "content")))

; .... well, so far when you update this atom, only the relevant rectangle gets redrawn, not all of 'em.
(comment
  (swap! state assoc 1 4)
  )



