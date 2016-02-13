(ns cljs-sandbox.core
  (:require [reagent.core :as r]
            [schema.core :as s])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [schema.core :as sm])
  (:use [cljs.core.async :only [chan <! >! put!]]))

(defn ^:export main []
  (r/render-component [:div.foo "hello"]
                      (js/document.getElementById "content")))


(comment
  )
