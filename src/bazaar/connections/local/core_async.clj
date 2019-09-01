(ns bazaar.connections.local.core-async
  (:require [bazaar.protocols :refer [Connection Lifecycle]]
            [clojure.core.async :as a]))

(defn create-from-channel
  [config]
  (assoc config :from-channel (a/chan)))

(defn create-to-channel
  [config]
  (assoc config :to-channel (a/chan)))

;; TODO: Change this to comp?
(defn start!
  [config]
  (-> config
      create-from-channel
      create-to-channel))

(defrecord CoreAsync [config]
  Connection
  (get-to-channel [this] (:to-channel this))
  (get-from-channel [this] (:from-channel this))
  Lifecycle
  (start! [this] 
          (assoc this :state (start! config)))
  (stop! [this] nil))