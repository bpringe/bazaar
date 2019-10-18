(ns bazaar.core
  (:require [bazaar.connections.local.core-async :refer [->CoreAsyncConnection]]
            [bazaar.processes.core-async :refer [->CoreAsyncProcess]]))

(def p1 (->CoreAsyncProcess
         {:name :p1
          :handler-fn (fn [msg] (assoc msg :p1 true))
          :in-conn (->CoreAsyncConnection
                    {:sub-topics ["in.p1"]})
          :out-conn (->CoreAsyncConnection
                     {:pub-topic "out.p1"})}))

(def p2 (->CoreAsyncProcess
         {:name :p2
          :handler-fn (fn [msg] (assoc msg :p2 true))
          :in-conn (->CoreAsyncConnection
                    {:sub-topics ["out.p1"]})
          :out-conn (->CoreAsyncConnection
                     {:pub-topic "out.p2"})}))

(def p3 (->CoreAsyncProcess
         {:name :p3
          :handler-fn (fn [msg] (assoc msg :p3 true))
          :in-conn (->CoreAsyncConnection
                    {:sub-topics ["out.p1"]})
          :out-conn (->CoreAsyncConnection
                     {:pub-topic "out.p3"})}))

(comment
  ;;;; Test process with connections, data flow
  
  (require '[clojure.core.async :as a]
           '[bazaar.protocols :as p])
  
  (def process-1 (p/start! p1))
  (def process-2 (p/start! p2))
  (def process-3 (p/start! p3))
  
  (def input-channel (p/get-input-channel (-> process-1 :state :in-conn)))
  (def p2-output-channel (p/get-output-channel (-> process-2 :state :out-conn)))
  (def p3-output-channel (p/get-output-channel (-> process-3 :state :out-conn)))

  (a/>!! input-channel {:a 1})

  (p/stop! process-1)
  (p/stop! process-2)
  (p/stop! process-3)

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
