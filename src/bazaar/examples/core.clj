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
  [[#'w2 #'p4]
   [#'p4 #'p5]])

(comment
  (require '[bazaar.workflow.core :as w]
           '[bazaar.runtime.core :as r]
           '[bazaar.connections.local.core-async :as ca])

  (r/up! {:workflow #'w3})

  (r/restart! {:workflow #'w3})

  (r/send! [:w3 :w2 :w1 :p1] {:hello "world"})

  (r/down!))