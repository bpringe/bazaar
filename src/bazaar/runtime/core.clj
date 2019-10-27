(ns bazaar.runtime.core
  (:require [bazaar.workflow.core :as w]
            [bazaar.protocols :as p]
            [clojure.spec.alpha :as s]))

(defonce state (atom {}))

(defn up! [runtime-config]
  (let [processes (w/get-processes (:workflow runtime-config))]
    (swap! state assoc :processes (reduce-kv (fn [processes k v]
                                               (println "Starting process" k)
                                               (assoc processes k (p/start! v)))
                                             {}
                                             processes))
    (println "Workflow" (-> processes first first first) "is running")))

(defn down! []
  (doseq [[process-key process] (:processes @state)]
    (println "Stopping process" process-key)
    (p/stop! process)
    (swap! state update :processes dissoc process-key)))


