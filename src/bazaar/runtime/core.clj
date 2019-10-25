(ns bazaar.runtime.core
  (:require [bazaar.workflow.core :as w]
            [bazaar.protocols :as p]))

(defonce state (atom {}))

(defn up!
  [runtime-config]
  (let [processes (w/get-processes (:workflow runtime-config))]
    (println processes)
    (reset! state (reduce-kv (fn [processes k v]
                               (println "Starting connection:" v)
                               (assoc processes k (p/start! v)))
                             {}
                             processes))))