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
  
  (require '[clojure.core.async :as a])
  
  (def process (start-process! p1))

  (def input-chan (p/get-from-channel (-> process :state :in-conn)))

  (def output-chan (p/get-to-channel (-> process :state :out-conn)))
  
  (a/put! input-chan "hello world")
  
  (a/<!! output-chan)
  
  (stop-process! process)

  ;;;; Test connection
  
  (require '[bazaar.connections.local.core-async :refer [->CoreAsync]]
           '[bazaar.protocols :as p]
           '[clojure.core.async :as a])

  (def conn (p/start! (->CoreAsync {:hello "world"})))

  (def from-channel (-> conn :state :from-channel))

  (def to-channel (-> conn :state :to-channel))

  (a/put! from-channel "hello world")

  (a/<!! to-channel)

  (p/stop! conn)


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
