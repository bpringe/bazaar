(ns bazaar.examples.core)

(defn p1
  [data]
  (assoc data :p1 true))

(defn p2
  [data]
  (assoc data :p2 true))

(defn p3
  [data]
  (assoc data :p3 true))

(defn p4
  [data]
  (assoc data :p4 true))

(defn p5
  [data]
  (assoc data :p5 true))

(defn p6
  [data]
  (assoc data :p6 true))

(def w1
  [[#'p1 #'p2]])

(def w2
  [[#'w1 #'p3]])

(def w3
  [[#'w1 #'p4]
   [#'w2 #'p5]
   #'p6])

(comment
  (require '[bazaar.workflow.core :as w])
  (require '[clojure.spec.alpha :as s])
  
  (w/get-processes #'w1))