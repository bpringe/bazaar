(ns bazaar.runtime.core
  (:require [bazaar.workflow.core :as w]
            [bazaar.protocols :as p]
            [clojure.core.async :as a]))

(defonce state (atom {}))

(defn up! [runtime-config]
  (let [workflow (:workflow runtime-config)]
    (if (= workflow (:running-workflow @state))
      (println "Workflow" workflow "is already running")
      (let [processes (w/get-processes (:workflow runtime-config))]
        (swap! state merge {:processes (reduce-kv (fn [processes k v]
                                                    (println "Starting process" k)
                                                    (assoc processes k (p/start! v)))
                                                  {}
                                                  processes)
                            :running-workflow workflow})
        (println "Workflow" workflow "is running")))))

(defn down! []
  (doseq [[process-key process] (:processes @state)]
    (println "Stopping process" process-key)
    (p/stop! process)
    (swap! state update :processes dissoc process-key))
  (swap! state dissoc :running-workflow))

(defn restart! [runtime-config]
  (down!)
  (up! runtime-config))

(defn send! [process-key data]
  (if-let [process (-> @state :processes (get process-key))]
    (let [input-channel (-> process :state :in-conn p/get-input-channel)]
      (a/>!! input-channel data))
    (println "Process" process-key "does not exist in the runtime")))
