(ns bazaar.core
  (:require [bazaar.connections.local.core-async :as lc]
            [bazaar.protocols :as p]
            [bazaar.processes.core-async :as proc]))

(defn start-connections!
  [config state]
  (println "Starting connections")
  (reduce-kv (fn [state k v]
               (if (or (= k :in-conn) (= k :out-conn))
                 (assoc state k (p/start! ((:factory-fn v) v)))
                 (assoc state k v)))
             {}
             config))

(defn start-process!
  [{:keys [factory-fn] :as config}]
  (->> {}
       (start-connections! config)
       factory-fn
       p/start!))

(def p1 {:name :p1
         :factory-fn proc/->CoreAsync
         :handler-fn (fn [msg] (assoc (:data msg) :p1 true))
         :in-conn {:factory-fn lc/->CoreAsync}
         :out-conn {:factory-fn lc/->CoreAsync}})



(comment
  ;;;; Test process with connections, data flow
  
  (def process (start-process! p1))

  (def input-chan (p/get-from-channel (-> process :state :in-conn)))

  (def output-chan (p/get-to-channel (-> process :state :out-conn)))
  
  (a/put! input-chan "hello world")
  
  (a/<!! output-chan)

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
