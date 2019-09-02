(ns bazaar.processes.core-async
  (:require [bazaar.protocols :as p]))

(defn start-connections!
  [config state]
  (reduce-kv (fn [state k v]
               (if (or (= k :in-conn) (= k :out-conn))
                 (assoc state k (p/start! ((:factory-fn v) v)))
                 (assoc state k v)))
             {}
             config))

(defn start-process!
  [config]
  (->> {}
       (start-connections! config)))

(defrecord CoreAsync [config]
  p/Lifecycle
  (start! [this] this)
  (stop! [this] this))