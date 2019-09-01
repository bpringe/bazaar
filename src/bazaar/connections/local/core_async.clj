(ns bazaar.connections.local.core-async
  (:require [bazaar.protocols :refer [Connection Lifecycle]]))

(defrecord CoreAsync [config]
  Connection
  (get-to-channel [this] (:to-channel this))
  (get-from-channel [this] (:from-channel this))
  Lifecycle
  (start! [this] nil)
  (stop! [this] nil))