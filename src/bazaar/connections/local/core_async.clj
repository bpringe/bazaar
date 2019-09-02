(ns bazaar.connections.local.core-async
  (:require [bazaar.protocols :refer [Connection Lifecycle]]
            [clojure.core.async :as a]))

(defn create-from-channel
  [config state]
  (assoc state :from-channel (a/chan)))

(defn create-to-channel
  [config state]
  (assoc state :to-channel (a/chan)))

(defn start-processing!
  [_ {:keys [from-channel to-channel] :as state}]
  (a/go-loop []
    (when-let [msg (a/<! from-channel)]
      (a/>! to-channel msg)
      (recur)))
  state)

(defn stop-from-channel!
  [{:keys [from-channel]}]
  (a/close! from-channel))

(defn stop-to-channel!
  [{:keys [to-channel]}]
  (a/close! to-channel))

(defn stop-connection!
  [state]
  (-> state
      stop-from-channel!
      stop-to-channel!))

;; TODO: Change this to comp?
(defn start-connection!
  "Starts the connection and returns the started connection."
  [config]
  (->> {}
       (create-from-channel config)
       (create-to-channel config)
       (start-processing! config)))

(defrecord CoreAsync [config]
  Connection
  (get-from-channel [this] 
                    (get-in this [:state :from-channel]))
  (get-to-channel [this]
                  (get-in this [:state :to-channel]))
  Lifecycle
  (start! [this] 
          (assoc this :state (start-connection! config)))
  (stop! [this]
         (stop-connection! (:state this))))