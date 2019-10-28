(ns bazaar.examples.throughput)

(defn p1
  [data]
  (assoc data :p1 true))

(defn p2
  [data]
  (Thread/sleep 5000)
  (assoc data :p2 true))

(def test-throughput
  [[#'p1 #'p2]])

(def runtime {:workflow #'test-throughput})

(comment
  (require '[bazaar.runtime.core :as r])

  (r/restart! runtime)

  (dotimes [n 10]
    (r/send! [:test-throughput :p1] {:hello (str "world " n)})))