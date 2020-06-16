(ns bazaar.examples.basic)

(defn p1
  [data]
  data)

(defn p2
  [data]
  data)

(defn p3
  [data]
  data)

(defn p4
  [data]
  data)

(defn p5
  [data]
  data)

(defn p6
  [data]
  data)

(def w1
  [[#'p1 #'p2]])

(def w2
  [[#'w1 #'p3]])

(def w3
  [[#'w2 #'p4]
   [#'p4 #'p5]
   [#'p4 #'p6]])

(comment
  (require '[bazaar.runtime.core :as r])

  (r/up! {:workflow #'w3})

  (r/restart! {:workflow #'w3})

  (r/send! [:w3 :w2 :w1 :p1] {:hello "world"})

  (r/down!))