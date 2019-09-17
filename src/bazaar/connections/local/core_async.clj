(ns bazaar.connections.local.core-async
  (:require [bazaar.protocols :refer [Connection Lifecycle]]
            [clojure.core.async :as a]))

(defn start-process-loop!
  [{:keys [from-channel to-channel] :as state}]
  (a/go-loop []
    (when-let [msg (a/<! from-channel)]
      (a/>! to-channel msg)
      (recur)))
  state)

(defn close-from-channel!
  [{:keys [from-channel] :as state}]
  (println "Closing from-channel")
  (a/close! from-channel)
  state)

(defn close-to-channel!
  [{:keys [to-channel] :as state}]
  (println "Closing to-channel")
  (a/close! to-channel)
  state)

(defrecord CoreAsync []
  Connection
  (get-from-channel [this] 
                    (get-in this [:state :from-channel]))
  (get-to-channel [this]
                  (get-in this [:state :to-channel]))
  Lifecycle
  (start! [this] 
          (assoc this :state (-> {}
                                 (assoc :from-channel (a/chan))
                                 (assoc :to-channel (a/chan))
                                 start-process-loop!)))
  (stop! [this]
         (-> (:state this)
             close-from-channel!
             close-to-channel!)))