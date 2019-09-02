(ns bazaar.processes.core-async
  (:require [bazaar.protocols :as p]))



(defn start-process!
  [config]
  (let [input-chan ()])
  (a/go-loop []
    ))

(defrecord CoreAsync [config]
  p/Lifecycle
  (start! [this]
          (assoc this :state (start-process! )))
  (stop! [this] this))