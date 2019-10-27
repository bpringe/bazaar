(ns bazaar.runtime.core
  (:require [bazaar.workflow.core :as w]
            [bazaar.protocols :as p]
            [clojure.spec.alpha :as s]
            [clojure.core.async :as a]))

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

(defn send!
  [process-key data]
  (if-let [process (-> @state :processes (get process-key))]
    (let [input-channel (-> process :state :in-conn p/get-input-channel)]
      (a/>!! input-channel data))
    (println "Process" process-key "does not exist in the runtime")))
