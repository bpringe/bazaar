(ns bazaar.protocols)

(defprotocol Lifecycle
  (start! [this])
  (stop! [this]))

(defprotocol Connection
  (get-to-channel [this])
  (get-from-channel [this]))