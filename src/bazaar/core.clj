(ns bazaar.core
  (:require [bazaar.connections.local.core-async :as lc]
            [bazaar.processes.core-async :as proc]))

(def p1 (proc/->CoreAsync
         {:name :p1
          :handler-fn (fn [msg] (assoc msg :p1 true))
          :in-conn (lc/->CoreAsync
                    {:sub-topics ["in.p1"]})
          :out-conn (lc/->CoreAsync
                     {:pub-topic "out.p1"})}))

(comment
  ;;;; Test process with connections, data flow
  
  (require '[clojure.core.async :as a]
           '[bazaar.protocols :as p])
  
  (def process (p/start! p1))
  
  (def input-channel (p/get-input-channel (-> process :state :in-conn)))
  
  (def output-channel (p/get-output-channel (-> process :state :out-conn)))

  (a/>!! input-channel {:a 1})
  
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
