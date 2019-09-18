(ns bazaar.core
  (:require [bazaar.connections.local.core-async :as lc]
            [bazaar.processes.core-async :as proc]))

(def p1 (proc/->CoreAsync
         {:name :p1
          :handler-fn (fn [msg] (assoc (:data msg) :p1 true))
          :in-conn (lc/->CoreAsync)
          :out-conn (lc/->CoreAsync)}))

(comment
  ;;;; Test process with connections, data flow
  
  (require '[clojure.core.async :as a]
           '[bazaar.protocols :as p])
  
  (def process (p/start! p1))
  
  (def input-channel (-> process :state :in-conn :state :input-channel))
  
  (def output-channel (-> process :state :out-conn :state :output-channel))

  (a/>!! input-channel "hello")
  
  (a/<!! output-channel)

  (p/stop! process)

  ;;;; pub/sub tests
  
  (require '[clojure.core.async :as a])

  (def cx (a/chan))

  (def cy (a/chan))

  (def cz (a/chan))

  (def cx-pub (a/pub cx (fn [_] "out.cx")))

  (a/sub cx-pub "out.cx" cy)

  (a/sub cx-pub "out.cx" cz)

  (a/>!! cx "testing")

  (a/<!! cy)

  (a/<!! cz))
