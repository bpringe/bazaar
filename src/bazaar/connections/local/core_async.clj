(ns bazaar.connections.local.core-async
  (:require [bazaar.protocols :refer [Connection Lifecycle]]
            [clojure.core.async :as a]))

(defn start-process-loop!
  [{:keys [input-channel output-channel] :as state}]
  (a/go-loop []
    (when-let [msg (a/<! input-channel)]
      (println "Message received in core-async connection:" msg)
      (println "Sending to output channel")
      (a/>! output-channel msg)
      (recur)))
  state)

(defn close-input-channel!
  [{:keys [input-channel] :as state}]
  (println "Closing input-channel")
  (a/close! input-channel)
  state)

(defn close-output-channel!
  [{:keys [output-channel] :as state}]
  (println "Closing output-channel")
  (a/close! output-channel)
  state)

(defrecord CoreAsync []
  Connection
  (get-input-channel [this]
    (get-in this [:state :input-channel]))
  (get-output-channel [this]
    (get-in this [:state :output-channel]))
  Lifecycle
  (start! [this]
    (assoc this :state (-> {}
                           (assoc :input-channel (a/chan))
                           (assoc :output-channel (a/chan))
                           start-process-loop!)))
  (stop! [this]
    (-> (:state this)
        close-input-channel!
        close-output-channel!)))

(comment
  (require '[bazaar.protocols :as p])
  
  (def conn (p/start! (->CoreAsync)))
  
  (def input-channel (p/get-input-channel conn))
  
  (def output-channel (p/get-output-channel conn))
  
  (a/>!! input-channel "hello")
  
  (a/<!! output-channel)
  
  1)