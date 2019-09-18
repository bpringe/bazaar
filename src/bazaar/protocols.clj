(ns bazaar.protocols)

(defprotocol Lifecycle
  (start! [this])
  (stop! [this]))

(defprotocol Connection
  (get-input-channel [this])
  (get-output-channel [this]))

(defprotocol Proc
  (get-name [this]))