(ns bazaar.connections.local.core-async
  (:require [bazaar.protocols :refer [Connection Lifecycle]]
            [clojure.core.async :as a]))

(defn create-from-channel
  [state config]
  (assoc state :from-channel (a/chan)))

(defn create-to-channel
  [state config]
  (assoc state :to-channel (a/chan)))

;; TODO: Change this to comp?
(defn start!
  [config]
  (-> {}
      (create-from-channel config)
      (create-to-channel config)))

(defrecord CoreAsync [config]
  Connection
  (get-from-channel [this] (get-in this [:state :from-channel]))
  (get-to-channel [this] (get-in this [:state :to-channel]))
  Lifecycle
  (start! [this] 
          (assoc this :state (start! config)))
  (stop! [this] nil))